package codec

import java.util.{Date, UUID}

import javax.inject.Inject
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.bson.{BsonReader, BsonType, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import persistence.{Movie, MovieId}
import play.api.Logger

import scala.collection.mutable.ArrayBuffer

/**
 * Big shout out to:
 * This SO post: https://stackoverflow.com/a/30655264/5189340
 * And their repository: https://github.com/desrepair/DutyScheduler
 * For useful examples in the right direction.
 * @param codecRegistry A regsitry of codecs for different types
 */
class MovieCodec (private val codecRegistry: CodecRegistry) extends Codec[Movie] {
  private val logger = Logger(getClass)

  override def getEncoderClass: Class[Movie] = { classOf[Movie] }

  override def encode(writer: BsonWriter, value: Movie, encoderContext: EncoderContext): Unit = {
    val dateCodec = codecRegistry.get(classOf[Date])
    writer.writeStartDocument()
      writer.writeString("id", value.id.underlying.toString)
      writer.writeString("title", value.title)
      writer.writeInt32("year", value.year)
      writer.writeString("rated", value.rated)
      writer.writeName("released")
      encoderContext.encodeWithChildContext(dateCodec, writer, value.released)
      writer.writeStartArray("genre")
        value.genre.foreach(it => writer.writeString(it))
      writer.writeEndArray()
    writer.writeEndDocument()
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Movie = {
    val dateCodec = codecRegistry.get(classOf[Date])

    reader.readStartDocument()
      // discard the ObjectId that gets serialized in the document
      // TODO: consider using this as the ID for a Movie object, rather than a UUID
      reader.readObjectId()
      val id = reader.readString("id")
      val title = reader.readString("title")
      val year = reader.readInt32("year")
      val rated = reader.readString("rated")
      reader.readName("released")
      val released = dateCodec.decode(reader, decoderContext)
      reader.readName("genre")
      val genre = new ArrayBuffer[String]()
      reader.readStartArray()
        // read the next BSON Type: if it's STRING, we're still in the array
        while(reader.readBsonType() == BsonType.STRING) {
          val next = reader.readString()
          genre.addOne(next)
        }
      reader.readEndArray()
    reader.readEndDocument()

    new Movie(MovieId(id), title, year, rated, released, genre.toSeq)
  }
}

/**
 * Since the MovieCodec needs an instance of the CodecRegistry
 * we need to delay its construction using a Provider that takes
 * its dependency as a parameter.
 */
class MovieCodecProvider @Inject()() extends CodecProvider {
  override def get[T](clazz: Class[T], registry: CodecRegistry): Codec[T] = {
    new MovieCodec(registry).asInstanceOf[Codec[T]]
  }
}

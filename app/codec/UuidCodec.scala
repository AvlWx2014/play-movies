package codec

import java.util.UUID

import javax.inject.Inject
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonWriter}
import play.api.Logger

class UuidCodec @Inject()() extends Codec[UUID] {
  private val logger = Logger(getClass)
  override def getEncoderClass: Class[UUID] = { classOf[UUID] }

  override def encode(writer: BsonWriter, value: UUID, encoderContext: EncoderContext): Unit = {
    writer.writeString(value.toString)
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): UUID = {
    UUID.fromString(reader.readString())
  }
}
package codec

import java.text.SimpleDateFormat
import java.util.Date

import javax.inject.Inject
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

/**
 * A custom BSON Codec for (de)serializing a java.util.Date (from) to a String.
 */
class DateCodec @Inject()() extends Codec[Date] {
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  override def getEncoderClass: Class[Date] = {
    classOf[Date]
  }

  override def encode(writer: BsonWriter, value: Date, encoderContext: EncoderContext): Unit = {
    writer.writeString(dateFormatter.format(value))
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Date = {
    dateFormatter.parse(reader.readString())
  }
}
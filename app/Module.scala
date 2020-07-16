import java.util.Date

import codec.{DateCodec, MovieCodecProvider}
import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.mongodb.scala.{MongoClient, MongoDatabase}
import persistence.{MovieDao, MovieRepository}
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[MovieRepository].to[MovieDao].in[Singleton]()
//    bind[Codec[UUID]].to[UuidCodec]
    bind[Codec[Date]].to[DateCodec]
    bind[CodecProvider].to[MovieCodecProvider]
  }

  @Provides
  def provideDatabase(codecRegistry: CodecRegistry): MongoDatabase = {
    // connect directly to localhost:27017 by default
    val mongoClient = MongoClient()
    mongoClient.getDatabase("demo").withCodecRegistry(codecRegistry)
  }

  @Provides
  def provideCodeRegistry(dateCodec: Codec[Date], movieCodecProvider: CodecProvider): CodecRegistry = {
//    val uuidCodec = new UuidCodec()
//    val dateCodec = new DateCodec()
//    val movieCodecProvider = new MovieCodecProvider()
    fromRegistries(
      fromCodecs(dateCodec),
      fromProviders(movieCodecProvider),
      MongoClient.DEFAULT_CODEC_REGISTRY
    )
  }
}

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{MongoClient, MongoDatabase}
import persistence.{Movie, MovieDao, MovieRepository}
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[MovieRepository].to[MovieDao].in[Singleton]()
  }

  @Provides
  def provideDatabase(codecRegistry: CodecRegistry): MongoDatabase = {
    // connect directly to port 27017 by default
    // here, the connect string is {scheme}://{hostname}
    // where hostname corresponds to a docker container name
    val mongoClient = MongoClient("mongodb://mongo")
    mongoClient.getDatabase("demo").withCodecRegistry(codecRegistry)
  }

  @Provides
  def provideCodeRegistry(): CodecRegistry = {
    fromRegistries(
      fromProviders(Macros.createCodecProvider(classOf[Movie])),
      MongoClient.DEFAULT_CODEC_REGISTRY
    )
  }
}

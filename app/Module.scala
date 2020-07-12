import com.google.inject.AbstractModule
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import persistence.{MovieRepository, TestMovieRepositoryImpl}
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule {
  override def configure() = {
    bind[MovieRepository].to[TestMovieRepositoryImpl].in[Singleton]()
  }
}

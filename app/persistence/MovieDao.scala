package persistence

import javax.inject.{Inject, Singleton}
import org.mongodb.scala.{Document, MongoDatabase}
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

@Singleton
class MovieDao @Inject()(database: MongoDatabase)(implicit ec: DataExecutionContext) extends AbstractMovieRepository {
  private val logger = Logger(getClass)

  /**
   * Return the MovieId so that the repository impl can use the return value to
   * to stick movies in the genre-based collections
   *
   * @param data
   * @param mc
   * @return
   */
  def add(data: Movie)(implicit mc: MarkerContext): Future[Movie] = {
    Future.failed(new NotImplementedError("Not Implemented"))
  }

  override def getAll()(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    val c = database.getCollection("movies")
    c.find[Movie]().toFuture()
  }

  override def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    Future.failed(new NotImplementedError("Not Implemented"))
  }

  override def get(name: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    Future.failed(new NotImplementedError("Not Implemented"))
  }

  override def delete(id: String)(implicit mc: MarkerContext): Future[Option[Movie]] = {
    Future.failed(new NotImplementedError("Not Implemented"))
  }
}

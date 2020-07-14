package persistence

import java.util.Date

import javax.inject.{Inject, Singleton}
import play.api.MarkerContext
import play.api.mvc.Results._
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

/**
 * TODO: implement the DatabaseApi
 * TODO: released as a Date?
 */
@Singleton
class MovieDao @Inject()()(implicit ec: DataExecutionContext) extends MovieRepository {
  /**
   * Return the MovieId so that the repository impl can use the return value to
   * to stick movies in the genre-based collections
   *
   * @param data
   * @param mc
   * @return
   */
  def add(data: Movie)(implicit mc: MarkerContext): Future[Movie] = ???

  override def getAll()(implicit mc: MarkerContext): Future[Iterable[Movie]] = ???

  override def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = ???

  override def get(name: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = ???

  override def delete(id: String)(implicit mc: MarkerContext): Future[Option[Movie]] = ???
}

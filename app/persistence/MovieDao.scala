package persistence

import java.util.Date

import javax.inject.Inject
import play.api.MarkerContext
import play.api.mvc.Results._
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

/**
 * Data transfer object representing a movie document.
 * @param title
 * @param year
 * @param rated
 * @param released
 * @param genre
 */
case class Movie(id: String, title: String, year: Int, rated: String, released: String, genre: Seq[String])

/**
 * TODO: implement the DatabaseApi
 * TODO: released as a Date?
 */
class MovieDao @Inject()()(implicit ec: DataExecutionContext) extends MovieRepository {
  /**
   * Return the MovieId so that the repository impl can use the return value to
   * to stick movies in the genre-based collections
   *
   * @param data
   * @param mc
   * @return
   */
//  def add(data: MovieData)(implicit mc: MarkerContext): Future[MovieId]

  override def list()(implicit mc: MarkerContext): Future[Iterable[MovieData]] = ???

  override def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[MovieData]] = ???

  override def get(name: String)(implicit mc: MarkerContext): Future[Iterable[MovieData]] = ???
}

package persistence

import java.util.UUID

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.{Logger, MarkerContext}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future

class MovieId private(val underlying: UUID) extends AnyVal {
  override def toString: String = underlying.toString
}

object MovieId {
  implicit val format: Format[MovieId] = Json.format

  def apply(raw: String): MovieId = {
    require(raw != null)
    new MovieId(UUID.fromString(raw))
  }

  def unapply(arg: MovieId): Option[String] = {
    Option(arg.underlying.toString)
  }
}

final case class MovieData(id: MovieId, title: String, year: Int, rated: String, released: String, genre: Seq[String])

object MovieData {
  implicit val format: Format[MovieData] = Json.format
}

trait MovieRepository {
  /**
   * Return the MovieId so that the repository impl can use the return value to
   * to stick movies in the genre-based collections
   * @param data
   * @param mc
   * @return
   */
  def add(data: Movie)(implicit mc: MarkerContext): Future[Movie]

  def getAll()(implicit mc: MarkerContext): Future[Iterable[Movie]]

  def get(title: String)(implicit mc: MarkerContext): Future[Iterable[Movie]]

  def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[Movie]]

  def delete(id: String)(implicit mc: MarkerContext): Future[Option[Movie]]
}

abstract class AbstractMovieRepository @Inject()(implicit ec: DataExecutionContext) extends MovieRepository {
  protected val dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
}

class DataExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "data.persistence")

@Singleton
class TestMovieRepositoryImpl @Inject()()(implicit ec: DataExecutionContext)
    extends AbstractMovieRepository {

  private val logger = Logger(getClass)

  private var repo = List(
    MovieData(MovieId("a74a68f7-c7cd-4cfe-b0f7-5694974d41e2"), "Pulp Fiction", 1994, "R", "10/14/1994", Seq("Crime", "Drama")),
    MovieData(MovieId("13b1e331-3e27-42fc-a2ff-00e3e07ba185"), "Goodfellas", 1990, "R", "09/21/1990", Seq("Biography", "Crime", "Drama")),
    MovieData(MovieId("c208af22-57dc-4589-90c0-00bd2096d9a5"), "Jurassic Park", 1993, "PG-13", "06/11/1993", Seq("Action", "Adventure", "Sci-Fi")),
    MovieData(MovieId("16fee969-0b99-4468-b380-5cc348408c22"), "Chicago", 2002, "PG-13", "01/24/2003", Seq("Comedy", "Crime", "Musical")),
    MovieData(MovieId("7c51ee06-c252-4291-ad28-e81bf096073a"), "Sleepless in Seattle", 1993, "PG", "06/25/1993", Seq("Comedy", "Drama", "Romance"))
  )

  /**
   * Return the MovieId so that the repository impl can use the return value to
   * to stick movies in the genre-based collections
   *
   * @param data
   * @param mc
   * @return
   */
//  override def add(data: MovieData)(implicit mc: MarkerContext): Future[MovieId] = ???

  override def getAll()(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    Future{
      logger.info(s"list: ")
      repo
    }
  }

  override def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    Future {
      logger.info(s"genre: $genre")
      repo.filter( data => data.genre contains genre)
    }
  }

  override def get(title: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    Future {
      logger.info(s"title: $title")
      repo.filter(data => data.title == title)
    }
  }
}

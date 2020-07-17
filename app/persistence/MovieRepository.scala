package persistence

import java.text.SimpleDateFormat
import java.util.{Date, Locale, UUID}

import akka.actor.ActorSystem
import controllers.MovieFormInput
import javax.inject.{Inject, Singleton}
import play.api.{Logger, MarkerContext}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json.{Format, JsPath, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

import scala.concurrent.Future
import scala.util.Try

final case class Movie(_id: ObjectId, title: String, year: Int, rated: String, released: Date, genre: Seq[String])

object Movie {
  implicit object dateFormat extends Format[Date] {
    private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    override def writes(date: Date): JsValue = {
      JsString(dateFormatter.format(date))
    }
    override def reads(json: JsValue): JsResult[Date] = {
      val try_ = Try[Date](dateFormatter.parse(json.as[String]))
      JsResult.fromTry(try_)
    }
  }

  implicit object objectIdFormatter extends Format[ObjectId] {
    override def reads(json: JsValue): JsResult[ObjectId] = {
      JsResult.fromTry(Try[ObjectId](new ObjectId(json.as[String])))
    }

    override def writes(o: ObjectId): JsValue = {
      JsString(o.toString)
    }
  }

  implicit val format: Format[Movie] = Json.format

  def apply(form: MovieFormInput): Movie = {
    require(form != null)
    val id = new ObjectId()
    new Movie(
      id,
      form.title,
      form.year,
      form.rated,
      form.released,
      form.genre
    )
  }
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
    Movie(new ObjectId("5f11ce8100a8d811a28e3cde"), "Pulp Fiction", 1994, "R", dateParser.parse("1994-10-14"), Seq("Crime", "Drama")),
    Movie(new ObjectId("5f11ceaa4a41c4b571f5b755"), "Goodfellas", 1990, "R", dateParser.parse("1990-09-21"), Seq("Biography", "Crime", "Drama")),
    Movie(new ObjectId("5f11ceaf156556a2ff532817"), "Jurassic Park", 1993, "PG-13", dateParser.parse("1993-06-11"), Seq("Action", "Adventure", "Sci-Fi")),
    Movie(new ObjectId("5f11ceb3cf9c4b83a1dc4e7c"), "Chicago", 2002, "PG-13", dateParser.parse("2003-01-24"), Seq("Comedy", "Crime", "Musical")),
    Movie(new ObjectId("5f11ceb792af170af673a224"), "Sleepless in Seattle", 1993, "PG", dateParser.parse("1993-06-25"), Seq("Comedy", "Drama", "Romance"))
  )

  /**
   * Return the MovieId so that the repository impl can use the return value to
   * to stick movies in the genre-based collections
   *
   * @param data
   * @param mc
   * @return
   */
  override def add(data: Movie)(implicit mc: MarkerContext): Future[Movie] = {
    Future {
      logger.info(s"New Movie: $data")
      val found = repo.find {
        case Movie(_, data.title, data.year, data.rated, data.released, data.genre) => true
        case _ => false
      }

      if (found.isDefined) {
        logger.info("Found a movie matching the submitted")
        found.get
      } else {
        logger.info("Movie not found. Adding!")
        repo = data :: repo
        data
      }
    }
  }

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

  override def delete(id: String)(implicit mc: MarkerContext): Future[Option[Movie]] = {
    Future {
      val uuid = UUID.fromString(id)
      val found = repo.find( data =>
        data.id.underlying == uuid
      )

      if (found.isDefined) {
        repo = repo diff List(found)
      }

      found
    }
  }

}

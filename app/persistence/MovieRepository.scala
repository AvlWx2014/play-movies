package persistence

import java.text.SimpleDateFormat
import java.util.{Date, Locale, UUID}

import akka.actor.ActorSystem
import controllers.MovieForm
import javax.inject.{Inject, Singleton}
import org.bson.codecs.ObjectIdGenerator
import org.bson.types.ObjectId
import play.api.{Logger, MarkerContext}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json.{Format, JsPath, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

import scala.concurrent.Future
import scala.util.Try

/**
 * The Movie Model
 * @param _id       An `org.bason.types.ObjectId` unique identifier
 * @param title     The movie title
 * @param year      The year the movie was released
 * @param rated     The movie rating
 * @param released  The release date of the movie
 * @param genre     Genre tags for the movie
 */
final case class Movie(_id: ObjectId, title: String, year: Int, rated: String, released: Date, genre: Seq[String])

/**
 * Companion object for our Movie model.
 */
object Movie {

  /**
   * An implicit JSON writer / reader for `java.util.Date`
   */
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

  /**
   * An implicit JSON writer / reader for `org.bson.types.ObjectId`
   */
  implicit object objectIdFormatter extends Format[ObjectId] {
    override def reads(json: JsValue): JsResult[ObjectId] = {
      JsResult.fromTry(Try[ObjectId](new ObjectId(json.as[String])))
    }

    override def writes(o: ObjectId): JsValue = {
      JsString(o.toString)
    }
  }

  /**
   * Implicit JSON writer / reader for the Movie model.
   */
  implicit val format: Format[Movie] = Json.format

  /**
   * Constructor for building a Movie from a MovieForm.
   *
   * This generates a new `org.bson.types.ObjectId` as a unique
   * identifier, rather than waiting for Mongo to generate one
   * on insert.
   *
   * @param form  A `MovieForm` parsed from a POST request
   * @return      A Movie created from the form input
   */
  def apply(form: MovieForm): Movie = {
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
   * Add a Movie to the repository.
   *
   * The implementation of this method should do it's best to
   * be idempotent.
   *
   * @param data  The movie being added
   * @param mc    An implicit MarkerContext for slf4j
   * @return      A `Future` with the inserted Movie
   */
  def add(data: Movie)(implicit mc: MarkerContext): Future[CreateMovieResult]

  /**
   * Get all Movies from the repository.
   * @param mc    An implicit MarkerContext for slf4j
   * @return      A `Future` with all the movies embedded
   */
  def getAll()(implicit mc: MarkerContext): Future[Iterable[Movie]]

  /**
   * Get Movies by title. If there's more than one movie with the
   * same title, they will all be returned here.
   * @param title   The Movie title to look up
   * @param mc      An implicit MarkerContext for slf4j
   * @return        A `Future` with all the Movies called `title`
   */
  def get(title: String)(implicit mc: MarkerContext): Future[Iterable[Movie]]

  /**
   * Get Movies by genre.
   *
   * TODO: this could be improved by adding additional repositories per genre
   *        and storing Movie IDs in those collections. Then the result would
   *        be a join of that genre collection, and the main movie collection.
   *
   * @param genre   The genre to look up
   * @param mc      An implicit MarkerContext for slf4j
   * @return        A `Future` with all the `genre` movies
   */
  def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[Movie]]

  /**
   * Remove a Movie from the repository.
   *
   * The implementations of this method should be idempotent, returning false
   * if there is no Movie with the given `id`
   * @param id    The ID of the Movie to be deleted
   * @param mc    An implicit MarkerContext for slf4j
   * @return      true if a Movie with ID `id` was found and deleted, otherwise false
   */
  def delete(id: String)(implicit mc: MarkerContext): Future[Boolean]
}

/**
 * A sealed type hierarchy to model the different result
 * scenarios related to creating a movie in an idempotent manner.
 *
 * This allows the controller to differentiate between different
 * results and return more representative HTTP status codes.
 *
 * New - A new movie entry was added to the repository successfully
 * Persisted - A movie with the same title, year, rating, and release
 *              date already exists in the repository - return that one
 * Failed - The repository failed to add the subject
 *
 * @param movie   The subject of the create request
 */
sealed abstract class CreateMovieResult(val movie: Movie)
case class New(override val movie: Movie) extends CreateMovieResult(movie)
case class Persisted(override val movie: Movie) extends CreateMovieResult(movie)
case class Failed(override val movie: Movie) extends CreateMovieResult(movie)

/**
 * An abstract base class for the MovieRepository trait that
 * provides a `SimpleDateFormatter` instance to child classes
 * for parsing `java.util.Date`s
 * @param ec      An implicit custom ExecutionContext to avoid repository
 *                operations on Play's main ExecutionContext
 */
abstract class AbstractMovieRepository @Inject()(implicit ec: DataExecutionContext) extends MovieRepository {
  protected val dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
}

/**
 * Custom ExecutionContext
 * @param actorSystem   The injected Akka ActorSystem
 */
class DataExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "data.persistence")

@Singleton
class TestMovieRepositoryImpl @Inject()()(implicit ec: DataExecutionContext)
    extends AbstractMovieRepository {

  private val logger = Logger(getClass)

  private var repo = List(
    Movie(
      new ObjectId("5f11ce8100a8d811a28e3cde"),
      "Pulp Fiction",
      1994,
      "R",
      dateParser.parse("1994-10-14"),
      Seq("Crime", "Drama")
    ),
    Movie(
      new ObjectId("5f11ceaa4a41c4b571f5b755"),
      "Goodfellas",
      1990,
      "R",
      dateParser.parse("1990-09-21"),
      Seq("Biography", "Crime", "Drama")
    ),
    Movie(
      new ObjectId("5f11ceaf156556a2ff532817"),
      "Jurassic Park",
      1993,
      "PG-13",
      dateParser.parse("1993-06-11"),
      Seq("Action", "Adventure", "Sci-Fi")
    ),
    Movie(
      new ObjectId("5f11ceb3cf9c4b83a1dc4e7c"),
      "Chicago",
      2002,
      "PG-13",
      dateParser.parse("2003-01-24"),
      Seq("Comedy", "Crime", "Musical")
    ),
    Movie(
      new ObjectId("5f11ceb792af170af673a224"),
      "Sleepless in Seattle",
      1993,
      "PG",
      dateParser.parse("1993-06-25"),
      Seq("Comedy", "Drama", "Romance")
    )
  )

  override def add(data: Movie)(implicit mc: MarkerContext): Future[CreateMovieResult] = {
    Future {
      logger.info(s"New Movie: $data")
      val found = repo.find {
        case Movie(_, data.title, data.year, data.rated, data.released, data.genre) => true
        case _ => false
      }

      if (found.isDefined) {
        logger.info("Found a movie matching the submitted")
        Persisted(found.get)
      } else {
        logger.info("Movie not found. Adding!")
        repo = data :: repo
        New(data)
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

  override def delete(id: String)(implicit mc: MarkerContext): Future[Boolean] = {
    Future {
      val found = repo.find( data =>
        data._id.toString == id
      )

      found match {
        case Some(_) =>
          repo = repo diff List(found)
          true
        case _ => false
      }
    }
  }

}

package persistence

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import org.bson.types.ObjectId
import org.mongodb.scala.{Document, MongoDatabase}
import play.api.{Logger, MarkerContext}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * An implementation of the `AbstractMovieRepository` that has a database backend.
 *
 * @param database  The database implementation, in this case MongoDatabase
 * @param ec      An implicit custom ExecutionContext to avoid repository
 *                operations on Play's main ExecutionContext
 */
@Singleton
class MovieDao @Inject()(database: MongoDatabase)(implicit ec: DataExecutionContext) extends AbstractMovieRepository {
  private val logger = Logger(getClass)
  private val collection = database.getCollection("movies")

  def add(data: Movie)(implicit mc: MarkerContext): Future[CreateMovieResult] = {
    logger.info(s"add() called in execution context ${Thread.currentThread().getName}")
    val document = Document(
      "_id" -> data._id,
      "title" -> data.title,
      "year" -> data.year,
      "rated" -> data.rated,
      "released" -> data.released,
      "genre" -> data.genre
    )

    logger.info(s"Awaiting find...")
    val option = Await.result(collection.find[Movie](
      Document(
        "title" -> data.title,
        "year" -> data.year,
        "rated" -> data.rated,
        "released" -> data.released
      ))
      .first()
      .toFutureOption(), Duration(1.0, TimeUnit.SECONDS))
    logger.info(s"Option returned: $option")

    option match {
      case None =>
        logger.info(s"No match found. Inserting...")
        collection.insertOne(document)
          .toFuture()
          .map { result =>
            if (result.wasAcknowledged()) {
              New(data)
            } else {
              Failed(data)
            }
          }
      case Some(_) =>
        logger.info("Found a match. Returning it...")
        Future.successful(Persisted(option.get))
    }
  }

  override def getAll()(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    logger.info("getAll: ")
    collection.find[Movie]().toFuture()
  }

  override def genre(genre: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    logger.info(s"genre: $genre")
    collection.find[Movie](Document("genre" -> genre)).toFuture()
  }

  override def get(name: String)(implicit mc: MarkerContext): Future[Iterable[Movie]] = {
    logger.info(s"get: $name")
    collection.find[Movie](Document("title" -> name)).toFuture()
  }

  override def delete(id: String)(implicit mc: MarkerContext): Future[Boolean] = {
    collection.deleteOne(Document("_id" -> new ObjectId(id))).toFuture().map {response =>
      response.getDeletedCount > 0
    }
  }
}

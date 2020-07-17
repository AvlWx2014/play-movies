package persistence

import javax.inject.{Inject, Singleton}
import org.bson.types.ObjectId
import org.mongodb.scala.{Document, MongoDatabase}
import play.api.{Logger, MarkerContext}
import org.mongodb.scala.model.Filters._
import org.reactivestreams.{Subscriber, Subscription}

import scala.concurrent.Future

@Singleton
class MovieDao @Inject()(database: MongoDatabase)(implicit ec: DataExecutionContext) extends AbstractMovieRepository {
  private val logger = Logger(getClass)
  private val collection = database.getCollection("movies")

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

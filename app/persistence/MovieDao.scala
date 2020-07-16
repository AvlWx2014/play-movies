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
//          collection.find().subscribe(new Subscriber[Document] {
      //      override def onSubscribe(s: Subscription): Unit = {
      //        logger.info(s"Subscribing: $s")
      //        // from the Reactive Mongo Tour
      //        // immediately request everything from the database
      //        // https://github.com/mongodb/mongo-java-driver-reactivestreams/blob/master/examples/tour/src/main/tour/SubscriberHelpers.java
      //        // send me everything
      //        // another value, like 10, would only get 10 emissions before completing
      //        s.request(Integer.MAX_VALUE)
      //      }
      //
      //      override def onNext(t: Document): Unit = {
      //        logger.info(s"Emission: $t")
      //      }
      //
      //      override def onError(t: Throwable): Unit = {
      //        logger.error(s"[!] Error: $t")
      //      }
      //
      //      override def onComplete(): Unit = {
      //        logger.info(s"Complete")
      //      }
      //    })
//    Future.successful(List())
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

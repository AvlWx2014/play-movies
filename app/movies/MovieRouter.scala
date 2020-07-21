package movies

import controllers.MovieController
import javax.inject.Inject
import play.api.Logger
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
 * A custom Router object that uses Play's SIRD
 * to parse query parameters and route those requests to
 * the appropriate controller methods.
 * @param controller    The Controller
 */
class MovieRouter @Inject()(controller: MovieController) extends SimpleRouter {
  val prefix = "/movies"
  val logger = Logger(getClass)

  override def routes: Routes = {
    case GET(p"/" ? q"title=$title") =>
      logger.info(s"GET $title")
      controller.title(title)

    case GET(p"/" ? q"genre=$genre") =>
      logger.info(s"GET $genre")
      controller.genre(genre)

    case POST(p"/") =>
      logger.info(s"POST")
      controller.create

    case DELETE(p"/" ? q"id=$id") =>
      logger.info(s"DELETE $id")
      controller.delete(id)

    case GET(p"/") =>
      logger.info(s"GET: /")
      controller.movies
  }
}

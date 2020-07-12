package controllers

import java.util.Date

import javax.inject.Inject
import persistence.{MovieDao, MovieRepository}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class MovieFormInput(title: String, year: Int, rated: String, released: Date, genre: Seq[String])

class MovieController @Inject()(
  dcc: DefaultControllerComponents,
  repo: MovieRepository
 )(implicit ec: ExecutionContext) extends BaseController {
  override protected def controllerComponents: ControllerComponents = dcc

  private val logger = Logger(getClass)

  /**
   * TODO: filtering based on genre and title should be done in a
   * TODO: custom Router rather than parsing query params here
   * @return
   */
  def movies: Action[AnyContent] = Action.async { implicit request =>
    val g = request.getQueryString("genre")
    val t = request.getQueryString("title")
    logger.info(s"movies: $g & $t")
    if ()
    repo.list().map { movies =>
      movies.filter(movie =>
        g match {
          case Some(_) => movie.genre contains g.get
          case None => true
        }
      ).filter(movie =>
        t match {
          case Some(_) => movie.title == t.get
          case None => true
        }
      )
    }.map {movies =>
      Ok(Json.toJson(movies))
    }
  }

  def genre(genre: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info("genre: $genre")
    repo.genre(genre).map { movies =>
      Ok(Json.toJson(movies))
    }
  }

  def title(title: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"title: $title")
    repo.get(title).map { movies =>
      Ok(Json.toJson(movies))
    }
  }
}

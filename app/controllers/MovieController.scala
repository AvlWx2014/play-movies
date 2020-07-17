package controllers

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import javax.inject.Inject
import persistence.{MovieDao, Movie, MovieRepository}
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class MovieFormInput(title: String, year: Int, rated: String, released: Date, genre: Seq[String])

object MovieFormInput {

  /**
   * Defining custom apply/unapply per this SO post for a seq
   * https://stackoverflow.com/a/27267673/5189340
   */
  def apply(title: String, year: Int, rated: String, released: Date, genre: String): MovieFormInput = {
    val seq = genre.split(",").toSeq
    new MovieFormInput(title, year, rated, released, seq)
  }

  def unapply(arg: MovieFormInput): Option[(String, Int, String, Date, String)] = {
    val str = arg.genre.mkString(",")
    Option(arg.title, arg.year, arg.rated, arg.released, str)
  }
}

class MovieController @Inject()(dcc: DefaultControllerComponents, repo: MovieRepository)(implicit ec: ExecutionContext)
    extends AbstractController(dcc)
    with play.api.i18n.I18nSupport {

  private val logger = Logger(getClass)

  private val form: Form[MovieFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "year" -> number,
        "rated" -> nonEmptyText,
        "released" -> date("yyyy-MM-dd"),
        "genre" -> text
      )(MovieFormInput.apply)(MovieFormInput.unapply)
    )
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"create: $request")
    processFormInput()
  }

  /**
   * TODO: filtering based on genre and title should be done in a
   * TODO: custom Router rather than parsing query params here
   * @return
   */
  def movies: Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"movies")
    repo.getAll().map { movies =>
      Ok(Json.toJson(movies))
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"delete: $id")
    repo.delete(id).map {
      case true => Ok
      case false => NotFound
    }
  }

  def genre(genre: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"genre: $genre")
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

  private def processFormInput[A]()(implicit request: Request[A]): Future[Result] = {
    def failure(bad: Form[MovieFormInput]) = {
      Future.successful(BadRequest(form.errorsAsJson))
    }

    def success(input: MovieFormInput) = {
      val data = Movie(input)
      repo.add(data).map { returned => Ok(Json.toJson(returned)) }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

package controllers

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import javax.inject.Inject
import persistence.{CreateMovieResult, Failed, Movie, MovieDao, MovieRepository, New, Persisted}
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class MovieForm(title: String, year: Int, rated: String, released: Date, genre: Seq[String])

object MovieForm {

  /**
   * Defining custom apply/unapply per this SO post for a seq
   * https://stackoverflow.com/a/27267673/5189340
   */
  def apply(title: String, year: Int, rated: String, released: Date, genre: String): MovieForm = {
    val seq = genre.split(",").toSeq
    new MovieForm(title, year, rated, released, seq)
  }

  def unapply(arg: MovieForm): Option[(String, Int, String, Date, String)] = {
    val str = arg.genre.mkString(",")
    Option(arg.title, arg.year, arg.rated, arg.released, str)
  }
}

class MovieController @Inject()(dcc: DefaultControllerComponents, repo: MovieRepository)(implicit ec: ExecutionContext)
    extends AbstractController(dcc)
    with play.api.i18n.I18nSupport {

  private val logger = Logger(getClass)

  private val form: Form[MovieForm] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "year" -> number,
        "rated" -> nonEmptyText,
        "released" -> date("yyyy-MM-dd"),
        "genre" -> text
      )(MovieForm.apply)(MovieForm.unapply)
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
    def failure(bad: Form[MovieForm]) = {
      Future.successful(BadRequest(form.errorsAsJson))
    }

    def success(input: MovieForm) = {
      val data = Movie(input)
      repo.add(data).map { result: CreateMovieResult =>
        result match {
          case New(_) => Created(Json.toJson(result.movie))
          case Persisted(_) => Ok(Json.toJson(result.movie))
          case Failed(_) => BadRequest(Json.toJson(result.movie))
        }
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

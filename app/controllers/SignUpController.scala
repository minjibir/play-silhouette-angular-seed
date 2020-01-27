package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.{ SignUpModel, User }
import models.services.UserService
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ AbstractController, Action, ControllerComponents }
import utils.auth.DefaultEnv
import scala.concurrent.{ ExecutionContext, Future }
import java.util.UUID

/**
 * The `Sign Up` controller.
 *
 * @param silhouette The Silhouette stack.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignUpController @Inject() (
  implicit
  ec: ExecutionContext,
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher)
  extends AbstractController(cc) {

  /**
   * Handles the submitted JSON data.
   *
   * @return The result to display.
   */
  def submit: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpModel].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(BadRequest(Json.toJson("message" -> "user already exists")))
        case None =>
          val authInfo = passwordHasher.hash(data.password)
          val user = User(
            userID = UUID.randomUUID(),
            loginInfo = loginInfo,
            firstName = Some(data.firstName),
            lastName = Some(data.lastName),
            fullName = Some(s"$data.firstName $data.lastName"),
            email = Some(data.email),
            avatarURL = None)
          for {
            avatar <- avatarService.retrieveURL(data.email)
            user <- userService.save(user.copy(avatarURL = avatar))
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
          } yield {
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            Ok(Json.toJson("token" -> token))
          }
      }
    }.recoverTotal(error =>
      Future.successful(Unauthorized(Json.toJson("message" -> s"invalid data $error"))))
  }
}

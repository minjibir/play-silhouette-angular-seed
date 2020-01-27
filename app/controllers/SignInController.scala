package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.Inject
import models.LoginModel
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{ AbstractController, Action, ControllerComponents }
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

/**
 * The `Sign In` controller.
 *
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param credentialsProvider    The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 */
class SignInController @Inject() (
  implicit
  ec: ExecutionContext,
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock)
  extends AbstractController(cc) {

  /**
   * Handles the submitted JSON data.
   *
   * @return The result to display.
   */
  def submit: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[LoginModel].map { data =>
      credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if data.rememberMe =>
              val c = configuration.underlying
              authenticator.copy(
                expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"))
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { token =>
              Ok(Json.toJson("token" -> token))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          //          case None => Future.failed(NotFound(Json.toJson("error" -> "Couldn't find user")))
        }
      }.recover {
        case e: ProviderException =>
          Unauthorized(Json.toJson("message" -> s"invalid credentials: $e"))
      }
    }.recoverTotal(error =>
      Future.successful(Unauthorized(Json.toJson("message" -> s"invalid credentials: $error"))))
  }
}

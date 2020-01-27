package controllers

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv
import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param silhouette             The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 */
class ApplicationController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry)
  extends AbstractController(cc) {

  /**
   * Returns the user.
   *
   * @return The result to display.
   */
  def user: Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request =>
      Future.successful(Ok(Json.toJson(request.identity)))
  }

  /**
   * Manages the sign out action.
   */
  def signOut: Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request =>
      silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
      silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }

  /**
   * Provides the desired template.
   *
   * @param template The template to provide.
   * @return The template.
   */
  def view(template: String) = silhouette.UserAwareAction {
    //    implicit request =>
    template match {
      //      case "home" => Ok(views.html.home())
      //      case "signUp" => Ok(views.html.signUp())
      //      case "signIn" => Ok(views.html.signIn(socialProviderRegistry))
      //      case "navigation" => Ok(views.html.navigation())
      case _ => NotFound
    }
  }
}

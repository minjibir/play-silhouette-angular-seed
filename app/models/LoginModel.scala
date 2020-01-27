package models

import play.api.libs.json.{ Format, Json }
// import play.api
/**
 * The object which handles the submission of the credentials.
 */
object LoginModel {
  implicit val loginFormModel: Format[LoginModel] = Json.format[LoginModel]
}

/**
 * The login data.
 *
 * @param email      The email of the user.
 * @param password   The password of the user.
 * @param rememberMe Indicates if the user should stay logged in on the next visit.
 */
case class LoginModel(email: String, password: String, rememberMe: Boolean = false) {
  require(!email.equals(""))
  require(!password.equals(""))
}

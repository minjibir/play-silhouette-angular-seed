package models

import play.api.libs.json.{ Format, Json }

/**
 * The class which holds the sign up data.
 */
object SignUpModel {
  implicit val jsonFormat: Format[SignUpModel] = Json.format[SignUpModel]
}

/**
 * The login data.
 *
 * @param firstName The first name of a user.
 * @param lastName  The last name of a user.
 * @param email     The email of the user.
 * @param password  The password of the user.
 */
case class SignUpModel(firstName: String, lastName: String, email: String, password: String) {
  require(!firstName.equals(""))
  require(!lastName.equals(""))
  require(!password.equals(""))
}

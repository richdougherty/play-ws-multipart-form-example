package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WSResponse
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {

  def index = Action.async {
    import java.io.File
    import com.ning.http.client.{ Request => AHCRequest, RequestBuilder }
    import com.ning.http.multipart._

    val ahcRequest: AHCRequest = new RequestBuilder("POST")
      .setUrl("http://localhost:9000/consume")
      .addBodyPart(new StringPart("string1", "foo"))
      .addBodyPart(new StringPart("string2", "bar"))
      .addBodyPart(new FilePart("file1", new File("/tmp/x"), "text/plain", "utf-8"))
      .addBodyPart(new FilePart("file2", new FilePartSource(new File("/tmp/y")), "text/plain", "utf-8"))
      .addBodyPart(new FilePart("file3", new ByteArrayPartSource("z", Array[Byte]('z'.toByte)), "text/plain", "utf-8"))
      .build()

    executeAhcRequest(ahcRequest).map { wsResponse =>
      Ok(wsResponse.body)
    }

  }

  def consume = Action { request =>
    Ok(request.body.asMultipartFormData.toString)
  }

  import com.ning.http.client.{ Request => AHCRequest }

  private def executeAhcRequest(ahcRequest: AHCRequest): Future[WSResponse] = {
    import play.api.Play.current
    import com.ning.http.client.{ AsyncCompletionHandler, AsyncHttpClient, Response => AHCResponse }
    import play.api.libs.ws.WS
    import play.api.libs.ws.ning.NingWSResponse
    import scala.concurrent.Promise
    val responsePromise = Promise[WSResponse]()
    val ahc: AsyncHttpClient = WS.client.underlying[AsyncHttpClient]
    ahc.executeRequest(ahcRequest, new AsyncCompletionHandler[Unit] {
      override def onCompleted(response: AHCResponse) = {
        responsePromise.success(NingWSResponse(response))
      }
      override def onThrowable(t: Throwable) = {
        responsePromise.failure(t)
      }
    })
    responsePromise.future
  }

}
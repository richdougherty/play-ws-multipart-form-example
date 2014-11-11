This demonstrates how to mix the Play WS library and the underlying AsyncHttpClient to upload multiple files using a `multipart/form-data` request.

To use, create the files `/tmp/x`, `/tmp/y` and `/tmp/z` then run the application and access http://localhost:9000. The application will upload the files to itself (at http://localhost:9000/consume) and then display the response.
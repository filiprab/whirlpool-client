package com.samourai.http.client;

public class HttpException extends Exception {
  private String responseBody;

  public HttpException(Exception cause, String responseBody) {
    super(cause);
    this.responseBody = responseBody;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

  public String getResponseBody() {
    return responseBody;
  }
}

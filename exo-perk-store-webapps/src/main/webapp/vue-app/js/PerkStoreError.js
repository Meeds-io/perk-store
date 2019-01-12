export function throwErrorFromServerCall(serverResponse, defaultErrorMessage) {
  if (!serverResponse || !serverResponse.ok) {
    const contentType = serverResponse && serverResponse.headers && serverResponse.headers.get('content-type');
    if (contentType && contentType.indexOf('application/json') !== -1) {
      return serverResponse.json().then((error) => {
        const message = getMessageFromServerError(error, defaultErrorMessage);
        throw new Error(message);
      });
    }
  }
  throw new Error(defaultErrorMessage);
}

export function getMessageFromServerError(error, defaultMessage) {
  if (!error || !error.code || !error.suffix || !error.message) {
    return defaultMessage;
  }
  return error.message;
}

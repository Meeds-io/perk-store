export function initCometd(settings) {
  const loc = window.location;
  cCometd.configure({
    url: `${loc.protocol}//${loc.hostname}${(loc.port && ':') || ''}${loc.port || ''}/${settings.userSettings.cometdContext}/cometd`,
    exoId: eXo.env.portal.userName,
    exoToken: settings.userSettings.cometdToken,
  });

  cCometd.subscribe(settings.userSettings.cometdChannel, null, (event) => {
    const data = event.data && JSON.parse(event.data);
    document.dispatchEvent(new CustomEvent(data.eventId, {detail : data && data.message}));
  });
}

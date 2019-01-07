export function initSettings() {
  return fetch(`/portal/rest/perkstore/api/account/settings`, {credentials: 'include'})
    .then((resp) => resp && resp.ok && resp.json())
    .then((settings) => (window.perkStoreSettings = settings ? settings : {}))
    .then(() => getSettings())
    .then((settings) => {
      if (settings) {
        window.perkStoreSettings = Object.assign(window.perkStoreSettings, settings);
      }
    });
}

export function getSettings() {
  return fetch(`/portal/rest/perkstore/api/settings`, {credentials: 'include'}).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting settings');
    }
  });
}

export function saveSettings(settings) {
  if (settings) {
    return fetch(`/portal/rest/perkstore/api/settings/save`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(settings),
    }).then((resp) => {
      if (resp && resp.ok) {
        return true;
      } else {
        throw new Error('Error saving settings', settings);
      }
    });
  } else {
    throw new Error('Error saving empty settings', settings);
  }
}

export function initSettings() {
  return fetch(`/portal/rest/perk-store/api/account/settings`, {credentials: 'include'})
    .then(resp =>  resp && resp.ok && resp.json())
    .then(settings =>  window.perkStoreSettings = settings ? settings : {})
    .then(() => getSettings())
    .then(settings =>  {
      if(settings) {
        window.perkStoreSettings = {...window.perkStoreSettings, ...settings};
      }
    });
}

export function getSettings() {
  return fetch(`/portal/rest/perk-store/api/settings`, {credentials: 'include'})
  .then(resp =>  resp && resp.ok && resp.json());
}

export function saveSettings(settings) {
  if(settings) {
    return fetch(`/portal/rest/perk-store/api/settings/save`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(settings)
    })
    .then(resp => resp && resp.ok);
  } else {
    return Promise.resolve(null);
  }
}


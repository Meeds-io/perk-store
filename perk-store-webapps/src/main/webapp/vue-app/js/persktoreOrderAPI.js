export function getPendingOrdersSize() {
    const filter = {notProcessed :true}
    return fetch('/portal/rest/perkstore/api/order/list?returnSize=true', {
        method: 'POST',
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(filter),
    }).then((resp) => {
      if (resp && resp.ok) { 
        return resp.json();
      } 
    });
}
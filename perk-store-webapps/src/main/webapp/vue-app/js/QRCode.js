export function generateBarCode(elementId, productId, orderId, userId) {
  const text = `@${productId}@${orderId}@${userId}@`;
  JsBarcode(`#${elementId}`, text, {
    displayValue: false
  });
}
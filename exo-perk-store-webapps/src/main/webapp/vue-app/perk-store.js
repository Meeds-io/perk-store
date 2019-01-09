import './../css/main.less';

import PerkStoreApp from './components/PerkStoreApp.vue';

const originalFetch = window.fetch;

const getParameter = (url, param) => {
  let urlPart;
  // eslint-disable-next-line no-useless-escape
  if (!param || !(url = url && url.trim()) || url.indexOf('?') < 0 || !(urlPart = url.match(new RegExp(`[\?&]{1}${param}=[^&#]*`)))) {
    return null;
  }
  return urlPart.length ? decodeURIComponent(urlPart[0].split('=')[1]) : null;
};

window.perkStoreProductsList = {
  1: {
    id: 1,
    title: 'Pre-fab homes',
    description: "Plusieurs variations de Lorem Ipsum peuvent être trouvées ici ou là, mais la majeure partie d'entre elles a été altérée par l'addition d'humour ou de mots aléatoires qui ne ressemblent pas une seconde à du texte standard. Si vous voulez utiliser un passage du Lorem Ipsum, vous devez être sûr qu'il n'y a rien d'embarrassant caché dans le texte. Tous les générateurs de Lorem Ipsum sur Internet tendent à reproduire le même extrait sans fin, ce qui fait de lipsum.com le seul vrai générateur de Lorem Ipsum. Iil utilise un dictionnaire de plus de 200 mots latins, en combinaison de plusieurs structures de phrases, pour générer un Lorem Ipsum irréprochable.",
    unlimited: false,
    purshased: 98,
    totalSupply: 100,
    price: 10,
    marchands: [
      {
        type: 'user',
        id: 'root',
      },
    ], // Notification receivers too
    receiverMarchand: {
      type: 'user',
      id: 'root',
    }, // Receiver wallet
    enabled: true,
    canEdit: true,
    maxOrdersPerUser: 10,
    orderPeriodicity: 'WEEK',
    orderPeriodicityLabel: 'Week',
    notProcessedOrders: 4,
    userOrders: {
      purchasedInCurrentPeriod: 2,
      totalOrders: 3,
    },
  },
  2: {
    id: 2,
    title: 'Favorite road trips',
    description: "On sait depuis longtemps que travailler avec du texte lisible et contenant du sens est source de distractions, et empêche de se concentrer sur la mise en page elle-même. L'avantage du Lorem Ipsum sur un texte générique comme 'Du texte. Du texte. Du texte.' est qu'il possède une distribution de lettres plus ou moins normale, et en tout cas comparable avec celle du français standard.",
    illustrationURL: 'http://localhost:8080/rest/private/jcr/repository/collaboration/Users/r___/ro___/roo___/root/Private/Pictures/road.jpg',
    unlimited: true,
    price: 20,
    marchands: [
      {
        type: 'user',
        id: 'root',
      },
    ], // Notification receivers too
    receiverMarchand: {
      type: 'user',
      id: 'root',
    }, // Receiver wallet
    enabled: true,
    canEdit: true,
  },
  3: {
    id: 3,
    title: 'Best airlines',
    description: "Le Lorem Ipsum est simplement du faux texte employé dans la composition et la mise en page avant impression. Le Lorem Ipsum est le faux texte standard de l'imprimerie depuis les années 1500, quand un imprimeur anonyme assembla ensemble des morceaux de texte pour réaliser un livre spécimen de polices de texte.",
    illustrationURL: 'http://localhost:8080/rest/private/jcr/repository/collaboration/Users/r___/ro___/roo___/root/Private/Pictures/plane.jpg',
    unlimited: false,
    purshased: 70,
    totalSupply: 100,
    price: 5,
    marchands: [
      {
        type: 'user',
        id: 'root',
      },
    ], // Notification receivers too
    receiverMarchand: {
      type: 'user',
      id: 'root',
    }, // Receiver wallet
    enabled: false,
    canEdit: false,
  },
};

window.perkStoreOrders = {
  1: {
    1: {
      id: 1,
      transactionHash: null,
      quantity: 2, // quantity ordered
      amount: 20, // amount sent
      sender: {
        type: 'user',
        id: 'root',
        technicalId: '1',
        displayName: 'Root Root',
      }, // type + id
      receiver: {
        type: 'space',
        id: 'test_space_renamed_2',
        technicalId: '7',
        displayName: 'Test space renamed 2',
      }, // type + id
      status: 'ORDERED',
      remainingQuantityToProcess: 2,
      deliveredQuantity: 0,
      refundedQuantity: 0,
      createdDate: Date.now(),
      deliveredDate: null,
      refundedDate: null,
    },
    2: {
      id: 2,
      transactionHash: '0x686cec7fbaad22f7af97dd77edd37c4c98d5bf672c7331a213dc4155082fc450',
      quantity: 1, // quantity ordered
      amount: 10, // amount sent
      sender: {
        type: 'user',
        id: 'root',
        technicalId: '1',
        displayName: 'Root Root',
      }, // type + id
      receiver: {
        type: 'space',
        id: 'test_space_renamed_2',
        technicalId: '7',
        displayName: 'Test space renamed 2',
      }, // type + id
      status: 'PAYED', // 'ordered', 'payed', 'delivered', 'canceled'
      remainingQuantityToProcess: 1,
      deliveredQuantity: 0,
      refundedQuantity: 0, // amount to refund
      createdDate: Date.now(),
      deliveredDate: null,
      refundedDate: null,
    },
    3: {
      id: 3,
      transactionHash: '0x686cec7fbaad22f7af97dd77edd37c4c98d5bf672c7331a213dc4155082fc450',
      quantity: 4, // quantity ordered
      amount: 40, // amount sent
      sender: {
        type: 'user',
        id: 'root',
        technicalId: '1',
        displayName: 'Root Root',
      }, // type + id
      receiver: {
        type: 'space',
        id: 'test_space_renamed_2',
        technicalId: '7',
        displayName: 'Test space renamed 2',
      }, // type + id
      status: 'DELIVERED', // 'ordered', 'payed', 'delivered', 'canceled'
      remainingQuantityToProcess: 1,
      deliveredQuantity: 3, // amount to refund
      refundedQuantity: 0, // amount to refund
      createdDate: Date.now(),
      deliveredDate: Date.now(),
      refundDate: Date.now(),
    },
    4: {
      id: 4,
      transactionHash: '0x686cec7fbaad22f7af97dd77edd37c4c98d5bf672c7331a213dc4155082fc450',
      quantity: 1, // quantity ordered
      amount: 10, // amount sent
      sender: {
        type: 'user',
        id: 'root',
        technicalId: '1',
        displayName: 'Root Root',
      }, // type + id
      receiver: {
        type: 'space',
        id: 'test_space_renamed_2',
        technicalId: '7',
        displayName: 'Test space renamed 2',
      }, // type + id
      status: 'CANCELED', // 'ordered', 'payed', 'delivered', 'canceled', 'refunded'
      remainingQuantityToProcess: 1,
      deliveredQuantity: 0, // amount to refund
      refundedQuantity: 0, // amount to refund
      createdDate: Date.now(),
      deliveredDate: null,
      refundedDate: null,
    },
    5: {
      id: 5,
      transactionHash: '0x686cec7fbaad22f7af97dd77edd37c4c98d5bf672c7331a213dc4155082fc450',
      quantity: 2, // quantity ordered
      amount: 20, // amount sent
      sender: {
        type: 'user',
        id: 'root',
        technicalId: '1',
        displayName: 'Root Root',
      }, // type + id
      receiver: {
        type: 'space',
        id: 'test_space_renamed_2',
        technicalId: '7',
        displayName: 'Test space renamed 2',
      }, // type + id
      status: 'REFUNDED', // 'ordered', 'canceled', 'payed', 'delivered', 'refunded'
      remainingQuantityToProcess: 0,
      deliveredQuantity: 0, // amount to refund
      refundedQuantity: 2, // amount to refund
      createdDate: Date.now(),
      deliveredDate: null,
      refundedDate: Date.now(),
    },
    6: {
      id: 6,
      transactionHash: '0x686cec7fbaad22f7af97dd77edd37c4c98d5bf672c7331a213dc4155082fc450',
      quantity: 2, // quantity ordered
      amount: 15, // amount sent
      sender: {
        type: 'user',
        id: 'root',
        technicalId: '1',
        displayName: 'Root Root',
      }, // type + id
      receiver: {
        type: 'space',
        id: 'test_space_renamed_2',
        technicalId: '7',
        displayName: 'Test space renamed 2',
      }, // type + id
      status: 'ERROR',
      error: "Payed amount doesn't match price",
      remainingQuantityToProcess: 1.5,
      deliveredQuantity: 0, // amount to refund
      refundedQuantity: 0, // amount to refund
      createdDate: Date.now(),
      deliveredDate: null,
      refundedDate: null,
    },
  },
};

window.fetch = (url, options) => {
  let resultJson = null;
  let resultText = null;

  if (url.indexOf('perkstore/api/product/list') >= 0) {
    resultJson = Object.values(window.perkStoreProductsList);
  } else if (url.indexOf('perkstore/api/product/save') >= 0) {
    const product = JSON.parse(options.body);
    if (!product.id) {
      product.id = Math.floor(Math.random() * 10000);
    }
    window.perkStoreProductsList[product.id] = product;
    resultJson = product;
  } else if (url.indexOf('perkstore/api/order/list') >= 0) {
    if (!options || !options.body) {
      throw new Error(`URL ${url} has empty parameters`, options);
    }
    const parameters = JSON.parse(options.body);
    const productId = parameters.productId;
    if(window.perkStoreOrders[productId]) {
      resultJson = Object.values(window.perkStoreOrders[productId]);
      resultJson = resultJson.filter(order => {
        if ((parameters.notProcessed && !order.remainingQuantityToProcess) || (!parameters.notProcessed && !parameters[order.status])) {
          return false;
        }
        if(parameters.searchInDates && parameters.selectedDate) {
          let foundDate = false;
          // Made on chain for reading simplicity
          if(order.createdDate && new Date(order.createdDate).toISOString().substr(0, 10) === parameters.selectedDate) {
            foundDate = true;
          }
          if(order.deliveredDate && new Date(order.deliveredDate).toISOString().substr(0, 10) === parameters.selectedDate) {
            foundDate = true;
          }
          if(!foundDate) {
            return false;
          }
        }
        return true;
      });
      
      if(parameters.limit) {
        resultJson = parameters.limit < resultJson.length ? resultJson.slice(0, parameters.limit) : resultJson;
      }
    } else {
      resultJson = [];
    }
  } else if (url.indexOf('perkstore/api/order/saveStatus') >= 0) {
    options.body = `?${options.body}`;
    const orderId = getParameter(options.body, 'orderId');
    const status = getParameter(options.body, 'status');
    const delivered = getParameter(options.body, 'delivered');
    const refunded = getParameter(options.body, 'refunded');

    const order = window.perkStoreOrders[1][Number(orderId)];
    if (status) {
      order.status = status;
    }
    if (delivered) {
      order.deliveredQuantity = delivered;
    }
    if (refunded) {
      order.refundedQuantity = refunded;
    }
    resultJson = order;
  } else if (url.indexOf('perkstore/api/order/save') >= 0) {
    const order = JSON.parse(options.body);
    order.id = Math.floor(Math.random() * 10000);
    order.sender = {
      type: 'user',
      id: eXo.env.portal.userName,
      technicalId: '1',
      displayName: 'Root Root',
    };
    order.receiver.technicalId = 7;
    order.receiver.displayName = 'Test space renamed 2';
    order.status = 'ORDERED';
    order.remainingQuantityToProcess = order.quantity;
    order.deliveredQuantity = 0;
    order.refundedQuantity = 0;
    order.createdDate = Date.now();

    if(!window.perkStoreOrders[order.productId]) {
      window.perkStoreOrders[order.productId] = {};
    }
    window.perkStoreOrders[order.productId][order.id] = order;
    resultJson = order;
  } else if (url.indexOf('perkstore/api/account/settings') >= 0) {
    // TODO Get account settings from sessionStorage
    resultJson = {};
  } else if (url.indexOf('perkstore/api/settings') >= 0) {
    resultJson = {
      isAdministrator: true,
      canAddProduct: true,
      symbol: 'Ȼ',
    };
  } else if (url.indexOf('perkstore/api/settings/save') >= 0) {
    // TODO Save general settings in sessionStorage
    resultText = 'saved';
  } else {
    return originalFetch(url, options);
  }
  return Promise.resolve(resultText ? new Response(resultText) : new Response(JSON.stringify(resultJson)));
};

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#PerkStoreApp',
  render: (h) => h(PerkStoreApp),
});

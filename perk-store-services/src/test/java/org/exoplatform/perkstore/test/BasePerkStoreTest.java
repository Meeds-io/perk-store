package org.exoplatform.perkstore.test;

import static org.exoplatform.perkstore.model.constant.ProductOrderTransactionStatus.NONE;
import static org.exoplatform.perkstore.model.constant.ProductOrderTransactionStatus.PENDING;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.perkstore.entity.ProductEntity;
import org.exoplatform.perkstore.entity.ProductOrderEntity;
import org.exoplatform.perkstore.model.*;
import org.exoplatform.perkstore.model.constant.*;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.utils.Utils;
import org.exoplatform.perkstore.storage.PerkStoreStorage;
import org.exoplatform.perkstore.storage.cached.PerkStoreCachedStorage;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.*;

public abstract class BasePerkStoreTest {

  protected static final String    USERNAME_ADMIN  = "root90";

  protected static final String    USERNAME        = "root1";

  protected static PortalContainer container;

  protected List<Serializable>     entitiesToClean = new ArrayList<>();

  private Random                   random          = new Random(1);

  @BeforeClass
  public static void beforeTest() throws Exception {
    container = PortalContainer.getInstance();
    assertNotNull("Container shouldn't be null", container);
    assertTrue("Container should have been started", container.isStarted());

    OrganizationService organizationService = getService(OrganizationService.class);
    UserImpl user = new UserImpl(USERNAME_ADMIN);
    GroupImpl group = new GroupImpl();
    group.setId(Utils.REWARDING_GROUP);
    MembershipTypeImpl role = new MembershipTypeImpl("*", null, null);
    organizationService.getMembershipHandler()
                       .linkMembership(user, group, role, false);
  }

  @Before
  public void beforeMethodTest() {
    RequestLifeCycle.begin(container);
  }

  @After
  public void afterMethodTest() {
    PerkStoreProductDAO productDAO = getService(PerkStoreProductDAO.class);
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);
    PerkStoreStorage perkStoreStorage = getService(PerkStoreStorage.class);

    assertDefaultSettingsAreSet();

    if (!entitiesToClean.isEmpty()) {
      for (Serializable entity : entitiesToClean) {
        if (entity instanceof ProductEntity) {
          productDAO.delete((ProductEntity) entity);
        } else if (entity instanceof Product) {
          Product product = (Product) entity;
          ProductEntity productEntity = productDAO.find(product.getId());
          if (productEntity != null) {
            productDAO.delete(productEntity);
          }
        } else if (entity instanceof ProductOrderEntity) {
          orderDAO.delete((ProductOrderEntity) entity);
        } else if (entity instanceof ProductOrder) {
          ProductOrder order = (ProductOrder) entity;
          ProductOrderEntity orderEntity = orderDAO.find(order.getId());
          if (orderEntity != null) {
            orderDAO.delete(orderEntity);
          }
        } else {
          throw new IllegalStateException("Entity not managed" + entity);
        }
      }
      if (perkStoreStorage instanceof PerkStoreCachedStorage) {
        ((PerkStoreCachedStorage) perkStoreStorage).clearCache();
      }
    }

    int productCount = productDAO.findAll().size();
    assertEquals("The previous test didn't cleaned product entities correctly, should add entities to clean into 'entitiesToClean' list.",
                 0,
                 productCount);

    int orderCount = orderDAO.findAll().size();
    assertEquals("The previous test didn't cleaned product order entities correctly, should add entities to clean into 'entitiesToClean' list.",
                 0,
                 orderCount);

    RequestLifeCycle.end();
  }

  protected static <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  private void assertDefaultSettingsAreSet() {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    GlobalSettings globalSettings = perkStoreService.getGlobalSettings();
    assertNotNull(globalSettings);
    assertTrue(globalSettings.getAccessPermissions() == null || globalSettings.getAccessPermissions().isEmpty()); // NOSONAR
    assertTrue(globalSettings.getManagers() == null || globalSettings.getManagers().isEmpty());
    assertTrue(globalSettings.getProductCreationPermissions() == null
        || globalSettings.getProductCreationPermissions().isEmpty());
    assertNull(globalSettings.getUserSettings());
    assertTrue(StringUtils.isBlank(globalSettings.getSymbol()));
  }

  protected Product newProduct(PerkStoreService perkStoreService, Product product) throws Exception {
    newProductInstance(product);

    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(savedProduct);
    return savedProduct;
  }

  protected Product newProductInstance(Product product) {
    product.setTitle("title");
    product.setIllustrationURL("IllustrationURL");
    product.setDescription("description");
    product.setReceiverMarchand(Utils.toProfile(10l));
    product.setAccessPermissions(Arrays.asList(Utils.toProfile(2l)));
    product.setMarchands(Arrays.asList(Utils.toProfile(1l)));
    product.setEnabled(true);
    product.setAllowFraction(false);
    product.setPrice(0.002d);
    product.setMaxOrdersPerUser(1);
    ProductOrderPeriodType[] values = ProductOrderPeriodType.values();
    product.setOrderPeriodicity(values[random.nextInt(values.length - 1)].getName());
    product.setUnlimited(false);
    product.setTotalSupply(10);
    return product;
  }

  protected ProductOrder newOrder(Product savedProduct) throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    ProductOrder productOrder = newOrderInstance(savedProduct);
    ProductOrder savedOrder = perkStoreService.createOrder(productOrder, USERNAME);
    entitiesToClean.add(0, savedOrder);
    return savedOrder;
  }

  protected ProductOrder newOrderInstance(Product savedProduct) {
    int quantity = 1;

    ProductOrder productOrder = new ProductOrder();
    productOrder.setProductId(savedProduct.getId());
    productOrder.setAmount(savedProduct.getPrice() * quantity);
    productOrder.setReceiver(savedProduct.getReceiverMarchand());
    productOrder.setSender(Utils.toProfile(1l));
    productOrder.setTransactionHash(generateRandomHash());
    productOrder.setTransactionStatus(PENDING.name());
    productOrder.setRefundTransactionStatus(NONE.name());
    productOrder.setQuantity(quantity);
    productOrder.setRemainingQuantityToProcess(5);
    return productOrder;
  }

  protected ProductEntity newProduct() {
    PerkStoreProductDAO productDAO = getService(PerkStoreProductDAO.class);

    ProductEntity productEntity = new ProductEntity();
    productEntity.setAllowFraction(false);
    productEntity.setCreatedDate(System.currentTimeMillis());
    productEntity.setCreator(1l);
    productEntity.setDescription("description");
    productEntity.setEnabled(true);
    productEntity.setIllustrationURL("illustrationURL");
    productEntity.setImages(new HashSet<Long>(Arrays.asList(1l, 2l)));
    productEntity.setLastModifiedDate(System.currentTimeMillis());
    productEntity.setLastModifier(1);
    // Used new ArrayList because the DAO.delete will modify the list and thus,
    // it shouldn't be unmodifiable list
    productEntity.setMarchands(new ArrayList<>(Arrays.asList(1l, 2l)));
    productEntity.setMaxOrdersPerUser(1);
    productEntity.setOrderPeriodicity(ProductOrderPeriodType.WEEK);
    productEntity.setPrice(0.002);
    productEntity.setReceiverId(1l);
    productEntity.setTitle("title");
    productEntity.setTotalSupply(1);
    productEntity.setUnlimited(false);

    productEntity = productDAO.create(productEntity);
    entitiesToClean.add(productEntity);

    return productEntity;
  }

  protected ProductOrderEntity newOrder(ProductEntity productEntity) {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);

    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    return orderEntity;
  }

  protected ProductOrderEntity newOrderInstance(ProductEntity productEntity) {
    ProductOrderEntity orderEntity = new ProductOrderEntity();
    orderEntity.setCreatedDate(System.currentTimeMillis());
    orderEntity.setDeliveredDate(0);
    orderEntity.setDeliveredQuantity(0);
    orderEntity.setProduct(productEntity);
    orderEntity.setQuantity(1);
    orderEntity.setReceiverId(productEntity.getReceiverId());
    orderEntity.setRefundedAmount(0);
    orderEntity.setRefundedDate(0);
    orderEntity.setRefundedQuantity(0);
    orderEntity.setRefundTransactionHash(null);
    orderEntity.setRefundTransactionStatus(ProductOrderTransactionStatus.NONE);
    orderEntity.setRemainingQuantity(1);
    orderEntity.setSenderId(2l);
    orderEntity.setStatus(ProductOrderStatus.ORDERED);
    orderEntity.setTransactionHash(generateRandomHash());
    orderEntity.setTransactionStatus(ProductOrderTransactionStatus.PENDING);
    return orderEntity;
  }

  protected String generateRandomHash() {
    StringBuilder hashStringBuffer = new StringBuilder("0x");
    for (int i = 0; i < 64; i++) {
      hashStringBuffer.append(Integer.toHexString(random.nextInt(16)));
    }
    return hashStringBuffer.toString();
  }

  protected void checkBasicOperations(Object object) {
    assertNotNull(object);
    try {
      assertNotNull(object.toString());
      assertNotNull(object.hashCode());
      assertEquals(object, object);
      if (object instanceof PerkStoreCloneable) {
        assertEquals(object, ((PerkStoreCloneable) object).clone());
      }
    } catch (Exception e) {
      fail("To string, equals and hashcode of object " + object.getClass().getSimpleName() + " shouldn't throw an exception"
          + e.getMessage());
    }
  }

}

package org.exoplatform.addon.perkstore.storage;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.addon.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.addon.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.model.constant.PerkStoreError;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderStatus;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class PerkStoreStorage {
  private static final String FILE_API_NAME_SPACE = "PerkStore";

  private static final int    DEFAULT_QUERY_LIMIT = 100;

  private static final Log    LOG                 = ExoLogger.getLogger(PerkStoreStorage.class);

  private FileService         fileService;

  private UploadService       uploadService;

  private PerkStoreProductDAO productDAO;

  private PerkStoreOrderDAO   orderDAO;

  public PerkStoreStorage(PerkStoreProductDAO perkStoreProductDAO, PerkStoreOrderDAO perkStoreOrderDAO) {
    this.productDAO = perkStoreProductDAO;
    this.orderDAO = perkStoreOrderDAO;
  }

  public Product getProductById(long productId) {
    ProductEntity entity = productDAO.find(productId);
    if (entity == null) {
      return null;
    } else {
      return parseProductFromEntity(entity);
    }
  }

  public Product saveProduct(Product product, String username) throws PerkStoreException {
    if (product == null) {
      throw new IllegalArgumentException("product argument is null");
    }

    saveProductImages(product.getId() == 0 ? null : getProductById(product.getId()), product, username);

    ProductEntity entity = toEntity(product);
    if (product.getId() == 0) {
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      if (identity == null) {
        throw new PerkStoreException(PerkStoreError.PRODUCT_CREATION_DENIED, username);
      }
      entity.setCreator(Long.parseLong(identity.getId()));
      entity.setCreatedDate(System.currentTimeMillis());
      entity.setId(null);
      entity = productDAO.create(entity);
    } else {
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      if (identity == null) {
        throw new PerkStoreException(PerkStoreError.PRODUCT_MODIFICATION_DENIED, username, product.getTitle());
      }
      entity.setLastModifier(Long.parseLong(identity.getId()));
      entity.setLastModifiedDate(System.currentTimeMillis());
      entity = productDAO.update(entity);
    }

    return parseProductFromEntity(entity);
  }

  public List<Product> getAllProducts() {
    List<ProductEntity> productEntities = productDAO.getAllProducts();
    // Used to enable cache usage
    return productEntities.stream().map(productEntity -> getProductById(productEntity.getId())).collect(Collectors.toList());
  }

  public double countOrderedQuantity(long productId) {
    if (productId == 0) {
      return 0;
    }
    double countOrderedQuantities = orderDAO.countOrderedQuantityByProductId(productId);
    if (countOrderedQuantities > 0) {
      return countOrderedQuantities - orderDAO.countRefundedQuantityByProductId(productId)
          - orderDAO.countOrderedQuantityByProductIdAndStatus(productId, ProductOrderStatus.CANCELED);
    } else {
      return 0;
    }
  }

  public long countRemainingOrdersToProcess(long id) {
    if (id == 0) {
      return 0;
    }
    return orderDAO.countRemainingOrdersToProcessByProductId(id);
  }

  public long countRemainingOrdersToProcess(long identityId, long productId) {
    if (identityId == 0 || productId == 0) {
      return 0;
    }
    return orderDAO.countRemainingOrdersByIdentityIdAndProductId(identityId, productId);
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    if (productId == 0) {
      return 0;
    }
    double userTotalPurchasedQuantity = orderDAO.countUserTotalPurchasedQuantity(productId, identityId);
    if (userTotalPurchasedQuantity > 0) {
      return userTotalPurchasedQuantity - orderDAO.countUserTotalRefundedQuantity(productId, identityId)
          - orderDAO.countUserTotalOrderedQuantityByStatus(productId, identityId, ProductOrderStatus.CANCELED);
    } else {
      return 0;
    }
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    if (productId == 0) {
      return 0;
    }
    double userTotalPurchasedQuantity = orderDAO.countUserPurchasedQuantityInPeriod(productId, identityId, startDate, endDate);
    if (userTotalPurchasedQuantity > 0) {
      return userTotalPurchasedQuantity - orderDAO.countUserRefundedQuantityInPeriod(productId, identityId, startDate, endDate)
          - orderDAO.countUserOrderedQuantityByStatusInPeriod(productId,
                                                              identityId,
                                                              startDate,
                                                              endDate,
                                                              ProductOrderStatus.CANCELED);
    } else {
      return 0;
    }
  }

  public List<ProductOrder> getOrders(String username, OrderFilter filter) {
    if (filter.getLimit() == 0) {
      filter.setLimit(DEFAULT_QUERY_LIMIT);
    }
    List<ProductOrderEntity> entities = orderDAO.getOrders(username, filter);
    return entities.stream().map(orderEntity -> getOrderById(orderEntity.getId())).collect(Collectors.toList());
  }

  public ProductOrder getOrderById(long orderId) {
    ProductOrderEntity orderEntity = orderDAO.find(orderId);
    return orderEntity == null ? null : fromEntity(orderEntity);
  }

  public ProductOrder saveOrder(ProductOrder order) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("order argument is null");
    }

    long productId = order.getProductId();
    ProductEntity productEntity = null;
    if (productId != 0) {
      productEntity = productDAO.find(productId);
    }
    if (productEntity == null) {
      throw new PerkStoreException(PerkStoreError.PRODUCT_NOT_EXISTS, productId);
    }

    ProductOrderEntity entity = toEntity(productEntity, order);
    if (order.getId() == 0) {
      entity.setCreatedDate(System.currentTimeMillis());
      entity.setId(null);
      entity = orderDAO.create(entity);
    } else {
      entity = orderDAO.update(entity);
    }

    return fromEntity(entity);
  }

  public ProductOrder findOrderByTransactionHash(String hash) {
    hash = formatTransactionHash(hash);
    ProductOrderEntity orderEntity = orderDAO.findOrderByTransactionHash(hash);
    return orderEntity == null ? null : fromEntity(orderEntity);
  }

  public ProductOrder findOrderByRefundTransactionHash(String hash) {
    hash = formatTransactionHash(hash);
    ProductOrderEntity orderEntity = orderDAO.findOrderByRefundTransactionHash(hash);
    return orderEntity == null ? null : fromEntity(orderEntity);
  }

  public FileDetail getFileDetail(long productId, Long imageAttachementId, boolean parseBinaryData) {
    FileItem fileItem;
    try {
      fileItem = getFileService().getFile(imageAttachementId);
    } catch (FileStorageException e) {
      LOG.warn("Error parsing image with id: {} on product with id {}", imageAttachementId, productId, e);
      return null;
    }
    if (fileItem == null || fileItem.getFileInfo() == null) {
      return null;
    }
    FileInfo fileInfo = fileItem.getFileInfo();
    FileDetail fileDetail = new FileDetail();
    fileDetail.setId(fileInfo.getId());
    fileDetail.setName(fileInfo.getName());
    fileDetail.setSize(fileInfo.getSize());
    fileDetail.setLastUpdated(fileInfo.getUpdatedDate() == null ? 0L : fileInfo.getUpdatedDate().getTime());

    if (parseBinaryData) {
      fileDetail.setData(fileItem.getAsByte());
    }

    fileDetail.setSrc(getImageSrc(productId, imageAttachementId));
    return fileDetail;
  }

  private Product parseProductFromEntity(ProductEntity entity) {
    Product product = fromEntity(entity);
    if (product != null && entity != null && entity.getImages() != null && !entity.getImages().isEmpty()) {
      long productId = product.getId();

      Set<FileDetail> imageFiles = new HashSet<>();
      Set<Long> imageAttachementIds = entity.getImages();
      for (Long imageAttachementId : imageAttachementIds) {
        try {
          FileDetail fileDetail = getFileDetail(productId, imageAttachementId, false);
          if (fileDetail == null) {
            continue;
          }
          imageFiles.add(fileDetail);
        } catch (Exception e) {
          LOG.warn("Error parsing image with id: {}", imageAttachementId, e);
        }
      }
      product.setImageFiles(imageFiles);
    }
    return product;
  }

  private String getImageSrc(long productId, long imageAttachementId) {
    return "/" + PortalContainer.getInstance().getName() + "/" + CommonsUtils.getRestContextName()
        + "/perkstore/api/product/" + productId + "/" + imageAttachementId;
  }

  private void saveProductImages(Product storedProduct, Product product, String username) {
    Set<FileDetail> storedImageFiles = storedProduct == null ? null : storedProduct.getImageFiles();
    Set<FileDetail> imageFilesToStore = product.getImageFiles();

    // Delete images
    if (storedImageFiles != null && !storedImageFiles.isEmpty()) {
      Set<FileDetail> imagesToDelete = new HashSet<>(storedImageFiles);
      if (imageFilesToStore != null && !imageFilesToStore.isEmpty()) {
        imagesToDelete.removeAll(imageFilesToStore);
      }
      for (FileDetail fileDetail : imagesToDelete) {
        long fileItemId = fileDetail.getId();
        if (fileItemId <= 0) {
          continue;
        }
        try {
          FileItem fileItem = getFileService().getFile(fileItemId);
          if (fileItem != null) {
            getFileService().deleteFile(fileItemId);
          }
        } catch (Exception e) {
          LOG.warn("Error deleting image {}", fileDetail.getName(), e);
        }
      }
    }

    // Store newly added images
    if (imageFilesToStore != null && !imageFilesToStore.isEmpty()) {
      Set<FileDetail> imagesToStore = new HashSet<>(imageFilesToStore);
      if (storedImageFiles != null && !storedImageFiles.isEmpty()) {
        imagesToStore.removeAll(storedImageFiles);
      }
      for (FileDetail fileDetail : imagesToStore) {
        long fileItemId = fileDetail.getId();
        if (fileItemId > 0 || fileDetail == null || fileDetail.getUploadId() == null) {
          continue;
        }
        try (InputStream inputStream = getUploadDataAsStream(fileDetail.getUploadId())) {
          if (inputStream == null) {
            continue;
          }
          FileItem fileItem = new FileItem(null,
                                           fileDetail.getName(),
                                           "image/png",
                                           FILE_API_NAME_SPACE,
                                           fileDetail.getSize(),
                                           new Date(),
                                           username,
                                           false,
                                           inputStream);
          fileItem = getFileService().writeFile(fileItem);
          fileDetail.setId(fileItem.getFileInfo().getId());
        } catch (Exception e) {
          LOG.warn("Error uploading image {}", fileDetail.getName(), e);
        }
      }
    }
  }

  private FileService getFileService() {
    if (fileService == null) {
      fileService = CommonsUtils.getService(FileService.class);
    }
    return fileService;
  }

  private UploadService getUploadService() {
    if (uploadService == null) {
      uploadService = CommonsUtils.getService(UploadService.class);
    }
    return uploadService;
  }

  private InputStream getUploadDataAsStream(String uploadId) throws FileNotFoundException {
    UploadResource uploadResource = getUploadService().getUploadResource(uploadId);
    if (uploadResource == null) {
      return null;
    } else {
      try { // NOSONAR
        return new FileInputStream(new File(uploadResource.getStoreLocation()));
      } finally {
        getUploadService().removeUploadResource(uploadId);
      }
    }
  }

}

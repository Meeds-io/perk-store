package org.exoplatform.perkstore.test.service.utils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.model.Profile;
import org.exoplatform.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.perkstore.notification.plugin.PerkStoreNotificationPlugin;
import org.exoplatform.perkstore.service.utils.NotificationUtils;
import org.exoplatform.perkstore.service.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.exoplatform.perkstore.service.utils.NotificationUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.xml.*", "org.xml.*" })
public class NotificationUtilsTest {

  @PrepareForTest({ Utils.class, NotificationInfo.class })

  @Test
  public void testSetNotificationRecipients() {
    PowerMockito.mockStatic(Utils.class);
    PowerMockito.mockStatic(NotificationInfo.class);
    List<String> groupMember = new ArrayList<>();
    groupMember.add("user");
    groupMember.add("user1");
    groupMember.add("user2");
    groupMember.add("user3");

    when(Utils.getRewardingGroupMember()).thenReturn(groupMember);
    try {
      GlobalSettings globalSettings = new GlobalSettings();
      Profile userProfile = new Profile(1L);
      Product product = new Product();
      userProfile.setId("user");
      product.setReceiverMarchand(userProfile);
      product.setMarchands(Arrays.asList(userProfile));
      product.setCreator(userProfile);
      NotificationInfo notification = new NotificationInfo();
      NotificationUtils.setNotificationRecipients(notification, globalSettings, product, null, true, false, userProfile);

      // should be just 3 since one of the group member is the creator
      assertNotNull(notification.getSendToUserIds());
      assertEquals(3, notification.getSendToUserIds().size());
    } catch (Exception e) {
    }
  }
}

package org.exoplatform.perkstore.test.service.utils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.perkstore.service.utils.Utils;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.exoplatform.perkstore.service.utils.Utils.REWARDING_GROUP;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.xml.*", "org.xml.*"})
public class UtilsTest {

  @PrepareForTest(CommonsUtils.class)
  @Test
  public void testGetRewardingGroupMembers() {
    PowerMockito.mockStatic(CommonsUtils.class);
    OrganizationService organizationService = mock(OrganizationService.class);
    when(CommonsUtils.getService(OrganizationService.class)).thenReturn(organizationService);
    GroupHandler groupHandler = mock(GroupHandler.class);
    MembershipHandler membershipHandler = mock(MembershipHandler.class);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);

    try {
      GroupImpl group = new GroupImpl();
      group.setId(Utils.REWARDING_GROUP);
      List<String> groupMembers = new ArrayList<>();
      when(groupHandler.findGroupById(REWARDING_GROUP)).thenReturn(group);
      when(membershipHandler.findAllMembershipsByGroup(group)).thenReturn(new ListAccessImpl(Membership.class, Collections.emptyList()));
      groupMembers = Utils.getRewardingGroupMembers();
      assertNotNull(groupMembers);
      assertEquals(0, groupMembers.size());

      List<Membership> memberships = new ArrayList<Membership>();

      MembershipImpl member1 = new MembershipImpl();
      member1.setMembershipType("member");
      member1.setUserName("user1");
      member1.setGroupId(REWARDING_GROUP);
      memberships.add(member1);
      MembershipImpl member2 = new MembershipImpl();
      member2.setMembershipType("*");
      member2.setUserName("user2");
      member2.setGroupId(REWARDING_GROUP);
      memberships.add(member2);

      MembershipImpl member3 = new MembershipImpl();
      member3.setMembershipType("*");
      member3.setUserName("user3");
      member3.setGroupId(REWARDING_GROUP);
      memberships.add(member3);

      ListAccess<Membership> membersShipList = new ListAccessImpl(Membership.class, memberships);
      when(membershipHandler.findAllMembershipsByGroup(group)).thenReturn(membersShipList);

      groupMembers = Utils.getRewardingGroupMembers();
      assertNotNull(groupMembers);
      assertEquals(3, groupMembers.size());
    } catch (Exception e) {
    }
  }
}

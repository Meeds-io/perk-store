<%@page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%@ page import="org.exoplatform.perkstore.model.OrderFilter"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.exoplatform.perkstore.service.PerkStoreService"%>
<%
  String title = "My orders";
  String titleOrders = "Orders";
  try {
    ResourceBundle bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.addon.PerkStore", request.getLocale());
    title = bundle.getString("exoplatform.perkstore.title.myOrders");
    titleOrders = bundle.getString("exoplatform.perkstore.title.orders");
  } catch (Exception e) {
    // Expected, when the title isn't translated to user locale
  }
  OrderFilter filter = new OrderFilter();
  filter.setNotProcessed(true);
  long totalOrders = ExoContainerContext.getService(PerkStoreService.class).countOrders(filter, request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="perkstoreOrderPortlet" flat="">
    <div class="v-application--wrap">
      <main>
        <div class="container pa-0">
          <div class="layout row wrap mx-0" style="cursor: pointer;">
            <div class="flex d-flex sx12">
              <div class="layout white row ma-0">
                <div class="flex d-flex xs12">
                  <div class="v-card v-card--flat v-sheet theme--light">
                    <div class="v-card__text subtitle-2 text-color text-sub-title pa-2">
                      <%=title%>
                    </div>
                  </div>
                </div>
                <div class="flex d-flex xs12 justify-center pa-2">
                  <a href="/portal/dw/perkstore/myorders" class="display-1 font-weight-bold big-number">
                    <%=totalOrders%><span class="mt-4 ms-1 product-label"><%=titleOrders%></span>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</div>
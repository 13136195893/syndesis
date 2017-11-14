import { Component, ChangeDetectorRef } from '@angular/core';
import { NotificationType } from 'patternfly-ng';

import { OAuthAppListItem } from './oauth-apps.component';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { OAuthApp, OAuthApps } from '../../model';
import { ModalService } from '../../common/modal/modal.service';
import { NotificationService } from 'app/common/ui-patternfly/notification-service';

@Component({
  selector: 'syndesis-oauth-app-modal',
  templateUrl: './oauth-app-modal.component.html'
})
export class OAuthAppModalComponent {
  // Holds the candidate for clearing credentials
  item: OAuthAppListItem;
  constructor(
    public store: OAuthAppStore,
    public detector: ChangeDetectorRef,
    private modalService: ModalService,
    private notificationService: NotificationService
  ) {}

  show(item: OAuthAppListItem) {
    this.item = item;
    this.modalService.show().then(
      modal =>
        modal.result
          ? this.removeCredentials()
              .then(app => (this.item.client = app))
              .then(_ =>
                this.popNotification({
                  type: NotificationType.SUCCESS,
                  header: 'Delete Successful',
                  message: 'Settings successfully deleted.'
                })
              )
              .catch(error =>
                this.popNotification({
                  type: NotificationType.DANGER,
                  header: 'Delete Failed',
                  message: `Failed to delete settings: ${error}`
                })
              )
              .then(_ => this.detector.markForCheck())
          : undefined
    );
  }

  // Clear the store credentials for the selected oauth app
  removeCredentials() {
    const app = { ...this.item.client, clientId: '', clientSecret: '' };
    return this.store
      .update(app)
      .take(1)
      .toPromise();
  }

  popNotification(notification) {
    this.notificationService.message(
      notification.type,
      notification.header,
      notification.message,
      false,
      null,
      []
    );
  }
}

import { Component, OnInit, ApplicationRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { FilterField, NotificationService, NotificationType } from 'patternfly-ng';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integrations } from '../../model';
import { ModalService } from '../../common/modal/modal.service';
import { IntegrationSupportService } from '../../store/integration-support.service';
import { ConfigService } from '../../config.service';
import { FileUploader, FileItem, ParsedResponseHeaders } from 'ng2-file-upload-base/src';

@Component({
  selector: 'syndesis-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class IntegrationsListPage implements OnInit {

  public uploader:FileUploader;

  loading: Observable<boolean>;
  integrations: Observable<Integrations>;
  filteredIntegrations: Subject<Integrations> = new BehaviorSubject(<Integrations>{});
  filterFields: Array<FilterField> = [{
    id: 'currentStatus',
    title: 'State',
    type: 'select',
    placeholder: 'Filter by state...',
    queries: [
      { id: 'actived', value: 'Activated' },
      { id: 'deactivated', value: 'Deactivated' },
      { id: 'draft', value: 'Draft' },
    ],
  }];

  constructor(
    private store: IntegrationStore,
    private modalService: ModalService,
    private integrationSupportService: IntegrationSupportService,
    public notificationService: NotificationService,
    public application: ApplicationRef,
    private config: ConfigService,
  ) {
    this.integrations = this.store.list;
    this.loading = this.store.loading;
    const url = this.config.getSettings().apiEndpoint + '/integration-support/import';
    this.uploader = new FileUploader({
      url: url,
      disableMultipart: true,
      autoUpload: true,
    });
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
      if( status === 204 ) {
        this.notificationService.message(
          NotificationType.SUCCESS,
          'Imported!',
          'Your integration has been imported',
          false, null, [],
        );
      } else {
        this.notificationService.message(
          NotificationType.DANGER,
          'Import Failed!',
          'Your integration could not be imported.',
          false, null, [],
        );
      }

    };
  }

  ngOnInit() {
    this.store.loadAll();
  }

  /**
   * Function that displays a modal for importing a database.
   */
  showImportDialog() {
    this.modalService.show('importIntegration').then(modal => {
      this.uploader.clearQueue();
    });
  }

}

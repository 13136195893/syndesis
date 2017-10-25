import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Actions, Action } from '../../../model';
import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ConnectorStore } from '../../../store/connector/connector.store';
import { Step, Connector } from '../../../model';
import { FlowPage } from '../flow-page';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integrations-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: ['./action-select.component.scss'],
})
export class IntegrationsSelectActionComponent extends FlowPage
  implements OnInit, OnDestroy {
  actions: Observable<Actions> = Observable.empty();
  filteredActions: Subject<Actions> = new BehaviorSubject(<Actions>{});
  connector: Observable<Connector>;
  loading: Observable<boolean>;
  routeSubscription: Subscription;
  actionsSubscription: Subscription;
  position: number;
  step: Step;

  constructor(
    public connectorStore: ConnectorStore,
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
    this.connector = connectorStore.resource;
    this.loading = connectorStore.loading;
    connectorStore.clear();
  }

  onSelected(action: Action) {
    log.debugc(() => 'Selected action: ' + action.name, category);
    this.currentFlow.events.emit({
      kind: 'integration-set-action',
      position: this.position,
      action: action,
      onSave: () => {
        this.router.navigate(['action-configure', this.position], {
          relativeTo: this.route.parent,
        });
      },
    });
  }

  goBack() {
    super.goBack(['connection-select', this.position]);
  }

  loadActions() {
    if (!this.currentFlow.loaded) {
      return;
    }
    const step = (this.step = this.currentFlow.getStep(this.position));
    if (!step) {
      // safety net
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent,
      });
      return;
    }
    if (!step.connection) {
      this.router.navigate(['connection-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    if (step.action) {
      this.router.navigate(['action-configure', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    this.connectorStore.load(step.connection.connectorId);
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.loadActions();
    }
  }

  ngOnInit() {
    this.actions = this.connector
      .filter(connector => connector !== undefined)
      .map(connector => connector.actions);
    this.actionsSubscription = this.actions.subscribe(_ => this.currentFlow.events
      .emit({
        kind: 'integration-action-select',
        position: this.position,
      }),
    );
    this.routeSubscription = this.route.params
      .pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        this.loadActions();
      })
      .subscribe();
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    if (this.actionsSubscription) {
      this.actionsSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}

import { Component, Input, ViewChild, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { PopoverDirective } from 'ngx-bootstrap/popover';

import { StepStore } from '../../../store/step/step.store';
import { ChildAwarePage } from '../child-aware-page';
import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step, Action } from '../../../model';
import { ModalService } from '../../../common/modal/modal.service';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-flow-view-step',
  templateUrl: './flow-view-step.component.html',
  styleUrls: ['./flow-view-step.component.scss']
})
export class FlowViewStepComponent extends ChildAwarePage {
  // the step object in the current flow
  @Input() step: Step;

  // the position in the integration flow
  @Input() position: number;

  // the current step in the flow the user is working with
  @Input() currentPosition: number;

  // the current state/page of the current step
  @Input() currentState: string;

  @ViewChild('pop') public pop: PopoverDirective;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    private stepStore: StepStore,
    private modalService: ModalService,
    private detector: ChangeDetectorRef
  ) {
    super(currentFlow, route, router);
  }

  getDeleteModalId() {
    return 'delete-integration-step-' + this.position;
  }

  showTooltip() {
    if (this.currentStepKind === 'mapper') {
      this.pop.show();
    }
  }

  hideTooltip() {
    this.pop.hide();
  }

  getStepKind(step) {
    if (step) {
      return step.stepKind;
    }
    return undefined;
  }

  getIconClass() {
    if (!this.step) {
      return 'fa fa-plus';
    }
    const step = this.step;
    switch (step.stepKind) {
      case 'endpoint':
        if (!this.step.connection) {
          return 'fa fa-plus';
        }
        return this.step.connection.icon
          ? 'fa ' + step.connection.icon
          : 'fa fa-airplane';
      case 'log':
        return 'fa fa-newspaper-o';
      default:
        return '';
    }
  }

  showDelete() {
    if (this.currentState !== 'save-or-add-step') {
      return false;
    }
    return true;
  }

  deletePrompt() {
    this.modalService
      .show('delete-integration-step-' + this.position)
      .then(modal => {
        if (modal.result) {
          this.deleteStep();
        }
      });
  }

  deleteStep() {
    const position = this.getPosition();
    const isFirst = position === this.currentFlow.getFirstPosition();
    const isLast = position === this.currentFlow.getLastPosition();

    this.currentFlow.events.emit({
      kind: 'integration-remove-step',
      position: position,
      onSave: () => {
        setTimeout(() => {
          try {
            this.detector.detectChanges();
          } catch (err) {
            // @TODO: Remove this try/catch once ChangeDetection is restored
          }
          if (isFirst || isLast) {
            this.router.navigate(['connection-select', position], {
              relativeTo: this.route
            });
          } else {
            this.router.navigate(['save-or-add-step'], {
              relativeTo: this.route
            });
          }
        }, 10);
      }
    });
  }

  getPosition() {
    let position = this.position;
    if (this.position === -1) {
      position = this.currentFlow.getLastPosition();
    }
    return position;
  }

  getParentClass() {
    let clazz = '';
    if (this.getPosition() === this.currentPosition) {
      //clazz = 'current';
      clazz = 'active';
    }
    const step = this.currentFlow.getStep(this.currentPosition);
    if (step && !this.stepIsComplete(step)) {
      clazz = clazz + ' disabled';
    }
    return 'parent-step ' + clazz;
  }

  getPropertyDefinitions(action: Action) {
    const definition: any = action.definition || {};
    const answer = definition.propertyDefinitionSteps || [];
    return answer;
  }

  getParentActiveClass() {
    let clazz = '';
    if (this.getPosition() === this.currentPosition) {
      clazz = 'active';
    } else {
      clazz = 'inactive';
    }
    return clazz;
  }

  getSubMenuActiveClass(state: string, page?: number) {
    if (!state) {
      if (this.thingIsEnabled(this.step)) {
        return 'active';
      } else {
        return 'inactive';
      }
    }
    let answer = 'inactive';
    if (
      (this.currentState === state || !state) &&
      this.getPosition() === this.currentPosition
    ) {
      answer = 'active';
    }
    if (this.getTextClass(state) !== 'active') {
      answer = answer + ' inactive';
    }
    const currentIndex = this.getCurrentStepIndex();
    if (page !== undefined && currentIndex >= 0) {
      if (page === currentIndex) {
        answer = 'active';
      } else {
        answer = 'inactive';
      }
    }
    return answer;
  }

  getConnectionClass() {
    if (this.step.stepKind === 'endpoint') {
      return '';
    }
    return 'not-connection';
  }

  getMenuCompleteClass(state: string) {
    switch (this.step.stepKind) {
      case 'endpoint':
        if (
          this.step.connection &&
          this.step.action &&
          this.step.configuredProperties
        ) {
          return 'complete';
        }
        break;
      default:
        if (this.step.stepKind && this.step.configuredProperties) {
          return 'complete';
        }
    }
    return 'incomplete';
  }

  getTextClass(state: string, page?: number) {
    switch (state) {
      case 'connection-select':
        if (this.step.connection) {
          return 'active';
        }
        break;
      case 'action-select':
        if (this.step.action) {
          return 'active';
        }
        break;
      case 'action-configure':
      case 'step-configure':
        if (this.step.configuredProperties) {
          return 'active';
        }
        break;
      case 'step-select':
        if (this.step.stepKind) {
          return 'active';
        }
        break;
      default:
        break;
    }
    if (
      (this.currentState === state || !state) &&
      this.getPosition() === this.currentPosition
    ) {
      return 'active';
    }
    return '';
  }

  stepIsComplete(step: Step) {
    switch (step.stepKind) {
      case 'endpoint':
        if (!step.connection || !step.action || !step.configuredProperties) {
          return false;
        }
        break;
      default:
        if (!step.stepKind || !step.configuredProperties) {
          return false;
        }
    }
    return true;
  }

  goto(page: string, index?: number) {
    if (!page) {
      if (!this.isCollapsed()) {
        // this means we're actually in this step, so don't change the view
        return;
      }
      // TODO wonder will there be more choices?
      switch (this.step.stepKind) {
        case 'endpoint':
          page = 'connection-select';
          break;
        default:
          page = 'step-select';
          break;
      }
    }
    // validate that the step is complete before we move
    const step = this.currentFlow.getStep(this.currentPosition);
    // step can be null if we're on the save or add step page
    if (step && !this.stepIsComplete(step)) {
      return;
    }
    const route = [page, this.getPosition()];
    if (index !== undefined) {
      route.push(index);
    }
    this.router.navigate(route, {
      relativeTo: this.route
    });
  }

  getStepText() {
    if (!this.step) {
      return 'Set up this step';
    }
    switch (this.step.stepKind) {
      case 'endpoint':
        if (this.step.action && this.step.action.name) {
          return this.step.action.name;
        }
        if (this.step.connection) {
          return this.step.connection.name;
        }
        if (this.getPosition() === 0) {
          return 'Start';
        }
        if (this.getPosition() === this.currentFlow.getLastPosition()) {
          return 'Finish';
        }
        return 'Set up this connection';
      default:
        if (this.step.name) {
          return this.step.name;
        }
        if (this.step.stepKind) {
          return this.stepStore.getStepName(this.step.stepKind);
        } else {
          return 'Set up this step';
        }
    }
  }

  isCollapsed() {
    return this.getPosition() !== this.currentPosition;
  }

  private thingIsEnabled(step: Step) {
    if (!step) {
      return false;
    }
    switch (step.stepKind) {
      case 'endpoint':
        if (step.connection && step.connection && step.configuredProperties) {
          return true;
        }
        break;
      default:
        if (step.configuredProperties) {
          return true;
        }
        break;
    }
    return false;
  }
}

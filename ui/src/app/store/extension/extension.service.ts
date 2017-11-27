import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Http, Response, ResponseContentType } from '@angular/http';
import { Restangular } from 'ngx-restangular';

import { RESTService } from '../entity/rest.service';
import { Extension, Extensions } from '../../model';

@Injectable()
export class ExtensionService extends RESTService<Extension, Extensions> {
  constructor(restangular: Restangular, private http: Http) {
    super(restangular.service('../v1beta1/extensions'), 'extension');
  }

  public getUploadUrl() {
    return this.restangularService.one().getRestangularUrl();
  }

  public importExtension(id: string): Observable<Response> {
    const url = this.restangularService.one(id).one('install').getRestangularUrl();
    return this.http.post(url, {});
  }

  public loadIntegrations(id: string): Observable<Response> {
    const url = this.restangularService.one(id).one('integrations').getRestangularUrl();
    return this.http.get(url);
  }

  public list(): Observable<Extensions> {
    return super.list().map( extensions => {
      return extensions.filter( extension => extension.status !== 'Deleted');
    });
  }
}

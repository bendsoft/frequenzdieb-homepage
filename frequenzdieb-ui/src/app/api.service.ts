import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  api_key = "";
  api_url = "https://virtserver.swaggerhub.com/bendsoft/frequenzdieb-rest/1.0.0";

  headers = new HttpHeaders();

  constructor(
    private http:HttpClient
  ) { }

  public getBlogEntries() {
    return this.http.get(`${this.api_url}/blog`, {
      headers: this.headers
    });
  }

  public getBlogEntry(id) {
    return this.http.get(this.api_url + "/blog/" + id);
  }

  public getSubscriptions() {
    return this.http.get(this.api_url);
  }


}

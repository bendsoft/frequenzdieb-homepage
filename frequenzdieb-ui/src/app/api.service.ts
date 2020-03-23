import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  api_key = "";
  // api_url = "https://virtserver.swaggerhub.com/bendsoft/frequenzdieb-rest/1.0.0a";
  api_url = "https://de0a780b-590b-47b3-82fa-33e56804d435.mock.pstmn.io";

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

  public getSubscription(email) {
    return this.http.get(this.api_url + "/subscription?email=" + email);
  }

  public getConcert() {
    return this.http.get(this.api_url + "/concert")
  }

  public postSubscription() {

  }

}

import { Service, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { tap } from 'rxjs';

interface AuthResponse {
  email: string;
  refreshToken: string;
  accessToken: string;
}

interface AccessTokenResponse {
  accessToken: string;
}

@Service()
export class Auth {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  register(email: string, password: string, firstName: string, lastName: string) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, { email, password, firstName, lastName })
      .pipe(tap((response) => this.storeTokens(response)));
  }

  login(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap((response) => this.storeTokens(response)));
  }

  refresh(token: string) {
    return this.http.post<AccessTokenResponse>(`${this.apiUrl}/refresh`, { token });
  }

  logout() {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.getAccessToken()}`);

    return this.http
      .post<void>(`${this.apiUrl}/logout`, { token: this.getRefreshToken() }, { headers })
      .pipe(tap(() => this.clearTokens()));
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  private storeTokens(response: AuthResponse) {
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
  }

  private getAccessToken() {
    return localStorage.getItem('accessToken');
  }

  private getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  private clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }
}

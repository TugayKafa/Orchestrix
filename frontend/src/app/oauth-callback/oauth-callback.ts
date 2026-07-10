import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Auth } from '../services/auth';

@Component({
  selector: 'app-oauth-callback',
  imports: [],
  template: '<p>Logging you in...</p>',
})
export class OauthCallback {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(Auth);

  constructor() {
    const accessToken = this.route.snapshot.queryParamMap.get('accessToken');
    const refreshToken = this.route.snapshot.queryParamMap.get('refreshToken');

    if (accessToken && refreshToken) {
      this.auth.storeTokens(accessToken, refreshToken);
    }

    this.router.navigateByUrl('/');
  }
}

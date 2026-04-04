import {Injectable} from '@angular/core';
import {HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Router} from '@angular/router';
import {tap} from 'rxjs/operators';
import {User} from 'src/app/model/user.model';

@Injectable()
export class XhrInterceptor implements HttpInterceptor {

    user = new User();

    constructor(private readonly router: Router) {
    }

    intercept(req: HttpRequest<any>, next: HttpHandler) {
        let httpHeaders = new HttpHeaders();
        if (sessionStorage.getItem('userdetails')) {
            this.user = JSON.parse(sessionStorage.getItem('userdetails')!);
        }
        if (this.user?.details.password && this.user.details.email) {
            httpHeaders = httpHeaders.append('Authorization', 'Basic ' + globalThis.btoa(this.user.details.email + ':' + this.user.details.password));
        } else {
            let authorization = sessionStorage.getItem('Authorization');
            if (authorization) {
                httpHeaders = httpHeaders.append('Authorization', authorization);
            }
        }

        let xsrf = sessionStorage.getItem('xsrf');
        if (xsrf) {
            httpHeaders = httpHeaders.append('X-XSRF-TOKEN', xsrf);
        }

        httpHeaders = httpHeaders.append('X-Requested-With', 'XMLHttpRequest');
        const xhr = req.clone({
            headers: httpHeaders
        });
        return next.handle(xhr).pipe(tap(
            (err: any) => {
                if (err instanceof HttpErrorResponse) {
                    if (err.status !== 401) {
                        return;
                    }
                    if (this.user.authDetails.roles.includes('ROLE_USER')) {
                        this.router.navigate(['dashboard']);
                    } else {
                        this.router.navigate(['home']);
                    }
                }
            }));
    }
}

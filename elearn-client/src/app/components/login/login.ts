import { Component, inject } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { LoginService } from "./login.service";
import { Router } from "@angular/router";

@Component({
  selector : "app-login",
  imports : [ReactiveFormsModule],
  templateUrl : "login.html"
})
export class Login {
  private fb = inject(FormBuilder);
  private loginService = inject(LoginService);
  private router = inject(Router);

  form = this.fb.group({
    usernameOrEmail : ['',[Validators.required]],
    password : ['',[Validators.required,Validators.minLength(8)]],
  })

  submit() {
    if (this.form.invalid) return;

    this.loginService.post(this.form.value).subscribe({
      next : (res) => {
        this.loginService.userProfile.set(res);
        this.router.navigate(["/dashboard"])
      },
      error : (err) => console.log("Error:", err)
    });

  }
}

import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import {Footer} from "./components/footer/footer";
import {Header} from "./components/header/header";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,Footer,Header],
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('elearn-client');
}

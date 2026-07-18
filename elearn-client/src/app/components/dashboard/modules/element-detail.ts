import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModuleService } from './modules.service';
import { Element } from '../../../models/models';

@Component({
  selector: 'app-element-detail',
  templateUrl: './element-detail.html'
})
export class ElementDetail implements OnInit {
  elements: Element[] = [];
  constructor(
    private svc: ModuleService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const moduleId = this.route.snapshot.paramMap.get('id');
    if (moduleId) {
      this.svc.getElements(moduleId).subscribe({
        next: (data) => {
          this.elements = data;
          this.cdr.detectChanges();
        }
      });
    }
  }
}


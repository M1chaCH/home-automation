import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {Observable, of} from "rxjs";

@Component({
  selector: 'app-light-config',
  templateUrl: './light-config.component.html',
  styleUrls: ['./light-config.component.scss']
})
export class LightConfigComponent implements OnInit{

  @Input() lightConfig!: LightConfigDTO;
  @Input() index!: number;
  editorOpen: boolean = false;

  @Input() openEditorId$: Observable<number> = of(-1);
  @Output() requestToggleOpen: EventEmitter<boolean> = new EventEmitter<boolean>();

  ngOnInit() {
    this.openEditorId$.subscribe(index => {
      this.editorOpen = !this.editorOpen;
      this.editorOpen = index === this.index && this.editorOpen;
    });
  }

  toggleOpen() {
    this.requestToggleOpen.next(!this.editorOpen);
  }

  getPreviewBackgroundColor(): string {
    return `rgb(${this.lightConfig.red}, ${this.lightConfig.green}, ${this.lightConfig.blue})`;
  }

  getPreviewOpacity(): string {
    return `${this.lightConfig.brightness / 100}`;
  }

  rename(newName: string) {
    console.warn("save not implemented yet", newName)
  }
}

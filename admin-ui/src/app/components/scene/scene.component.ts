import {Component, Input} from '@angular/core';
import {SceneDTO} from "../../dtos/scene/SceneDTO";
import {Subject} from "rxjs";

@Component({
  selector: 'app-scene',
  templateUrl: './scene.component.html',
  styleUrls: ['./scene.component.scss']
})
export class SceneComponent {

  @Input() scene!: SceneDTO;
  openLightConfigEditorSubject: Subject<number> = new Subject<number>();
  private openEditorIndex: number = -1;

  toggleOpen(index: number) {
    let indexToSend: number = this.openEditorIndex !== index ? index : -1;
    console.log("sending next id", indexToSend)
    this.openLightConfigEditorSubject.next(indexToSend);
    this.openEditorIndex = indexToSend;
  }

  renameScene(newName: string): void {
    console.warn("save not implemented yet", newName)
  }
}

import {Component, EventEmitter, HostListener, Input, Output} from '@angular/core';
import {SimpleSceneDTO} from "../../dtos/scene/SceneDTO";

@Component({
  selector: 'app-scene-selector',
  templateUrl: './scene-selector.component.html',
  styleUrls: ['./scene-selector.component.scss']
})
export class SceneSelectorComponent {
  private readonly MAX_SHOWN_SCENES: number = 8;

  @Input() simpleScenes: SimpleSceneDTO[] = [];
  @Input() selectedScene: SimpleSceneDTO | undefined;
  @Output() selectedSceneChange: EventEmitter<SimpleSceneDTO> = new EventEmitter<SimpleSceneDTO>();

  filteredScenes: SimpleSceneDTO[] = [];
  query: string = "";
  open: boolean = false;

  @Input() set scenesAndSelected(data: { scenes: SimpleSceneDTO[], selected: string }) {
    this.simpleScenes = data.scenes;
    this.selectedScene = this.simpleScenes.find(s => s.name === data.selected);
  }

  @HostListener('document:keydown.escape')
  escEventListener() {
    this.closeDialog();
  }

  @HostListener('document:keydown.enter',  ['$event'])
  enterEventListener(event: KeyboardEvent) {
    event.preventDefault();
  }

  toggleDialog(): void {
    if(this.open) {
      this.closeDialog()
    } else {
      this.filteredScenes = this.simpleScenes?.slice(0, this.MAX_SHOWN_SCENES) || [];
      this.open = true;
    }
  }

  queryChanged(query: string): void {
    query = query.toLowerCase();
    this.filteredScenes = this.simpleScenes?.filter(r => r.name.toLowerCase().includes(query)) || [];
    this.filteredScenes = this.filteredScenes.slice(0, this.MAX_SHOWN_SCENES);
  }

  resourceSelected(scene?: SimpleSceneDTO): void {
    if(!scene){
      if(this.filteredScenes.length < 1)
        return;
      scene = this.filteredScenes[0];
    }

    this.selectedScene = scene;
    this.selectedSceneChange.emit(scene);
    this.closeDialog();
  }

  removeScene(): void {
    this.selectedScene = undefined;
    this.selectedSceneChange.emit(undefined);
    this.closeDialog();
  }

  closeDialog() {
    this.query = "";
    this.queryChanged("");
    this.open = false;
  }
}

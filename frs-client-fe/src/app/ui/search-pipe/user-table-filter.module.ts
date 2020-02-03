import {NgModule} from "@angular/core";
import {UserTableFilterPipe} from "./user-table-filter.pipe";

@NgModule({
  declarations: [UserTableFilterPipe],
  exports: [UserTableFilterPipe]
})
export class UserTablePipeModule {}
